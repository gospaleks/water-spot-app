package rs.gospaleks.waterspot.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import rs.gospaleks.waterspot.data.mapper.toDomain
import rs.gospaleks.waterspot.data.model.FirestoreReviewDto
import rs.gospaleks.waterspot.data.model.FirestoreSpotDto
import rs.gospaleks.waterspot.domain.model.Review
import rs.gospaleks.waterspot.domain.model.ReviewWithUser
import rs.gospaleks.waterspot.domain.model.Spot
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.domain.model.User
import javax.inject.Inject

class FirestoreSpotDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveSpot(spotDataMap: Map<String, Any?>) : Result<Unit> {
        return try {
            firestore.collection("spots")
                .add(spotDataMap) // generates a new document ID
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun getAllSpots(): Flow<Result<List<Spot>>> = callbackFlow {
        val spotsCollection = firestore.collection("spots")
        val listener = spotsCollection
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val spots = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(FirestoreSpotDto::class.java)
                            ?.copy(id = doc.id)
                            ?.toDomain()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                } ?: emptyList()

                trySend(Result.success(spots))
            }

        awaitClose { listener.remove() }
    }

    fun getAllSpotsWithUsers(): Flow<Result<List<SpotWithUser>>> = callbackFlow {
        val spotsCollection = firestore.collection("spots")
        val listener = spotsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val spotDtos = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(FirestoreSpotDto::class.java)
                            ?.copy(id = doc.id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                } ?: emptyList()

                val spots = spotDtos.map { it.toDomain() }

                // Launch a coroutine to fetch users and combine
                CoroutineScope(Dispatchers.IO).launch {
                    val spotsWithUsers = spots.map { spot ->
                        try {
                            val userSnapshot = firestore.collection("users")
                                .document(spot.userId)
                                .get()
                                .await()

                            val user = userSnapshot.toObject(User::class.java)

                            SpotWithUser(spot, user)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            SpotWithUser(spot) // Return spot without user if error occurs
                        }
                    }
                    trySend(Result.success(spotsWithUsers))
                }
            }

        awaitClose { listener.remove() }
    }

    fun getAllReviewsForSpotWithUsers(spotId: String): Flow<Result<List<ReviewWithUser>>> = callbackFlow {
        val reviewsCollection = firestore.collection("spots")
            .document(spotId)
            .collection("reviews")

        val listener = reviewsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val reviewDtos = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(FirestoreReviewDto::class.java)
                            ?.copy(userId = doc.id)
                            ?.toDomain()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                } ?: emptyList()

                CoroutineScope(Dispatchers.IO).launch {
                    val reviewsWithUsers = reviewDtos.mapNotNull { review ->
                        try {
                            val userSnapshot = firestore.collection("users")
                                .document(review.userId)
                                .get()
                                .await()

                            val user = userSnapshot.toObject(User::class.java)

                            if (user != null) {
                                ReviewWithUser(review, user)
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    trySend(Result.success(reviewsWithUsers))
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun getSpotById(id: String): Result<Spot?> {
        return try {
            val document = firestore.collection("spots").document(id).get().await()
            if (document.exists()) {
                val spotDto = document.toObject(FirestoreSpotDto::class.java)
                Result.success(spotDto?.copy(id = document.id)?.toDomain())
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun addReviewToSpot(spotId: String, review: Map<String, Any?>): Result<Unit> {
        val userId = review["userId"] as String
        val rating = review["rating"] as Int

        val reviewRef = firestore.collection("spots")
            .document(spotId)
            .collection("reviews")
            .document(userId)

        val spotRef = firestore.collection("spots").document(spotId)

        return try {
            val existingReview = reviewRef.get().await()

            if (existingReview.exists()) {
                Result.failure(Exception("Review already exists for this user"))
            } else {
                reviewRef.set(review).await()

                // Azuriraj prosecnu ocenu i broj review-a
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(spotRef)
                    val currentAvg = snapshot.getDouble("averageRating") ?: 0.0
                    val currentCount = snapshot.getLong("reviewCount")?.toInt() ?: 0

                    val newCount = currentCount + 1
                    val newAvg = ((currentAvg * currentCount) + rating) / newCount

                    transaction.update(spotRef, mapOf(
                        "averageRating" to newAvg,
                        "reviewCount" to newCount
                    ))
                }.await()

                // TODO: Dodaj poene korisniku za ostavljenu recenziju

                Result.success(Unit)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}