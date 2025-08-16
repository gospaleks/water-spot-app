package rs.gospaleks.waterspot.data.remote.firebase

import android.util.Log
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import rs.gospaleks.waterspot.data.mapper.toDomain
import rs.gospaleks.waterspot.data.model.FirestoreReviewDto
import rs.gospaleks.waterspot.data.model.FirestoreSpotDto
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

    fun getAllSpotsWithUsers(
        center: GeoLocation = GeoLocation(43.1571, 22.5840),
        radius: Double = 10_000.0 // u metrima
    ): Flow<Result<List<SpotWithUser>>> = flow {
        val result: Result<List<SpotWithUser>> = try {
            Log.d("FirestoreSpotDataSource", "Fetching spots within radius: $radius meters from center: $center")
            val spotsCollection = firestore.collection("spots")

            // 1. Pripremi upite po hash boundovima
            val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radius)
            val tasks = bounds.map { bound ->
                spotsCollection
                    .orderBy("geohash")
                    .startAt(bound.startHash)
                    .endAt(bound.endHash)
                    .get()
            }

            // 2. Sačekaj sve upite
            val snapshots = Tasks.whenAllSuccess<QuerySnapshot>(tasks).await()

            val matchingDocs = mutableListOf<DocumentSnapshot>()
            for (snap in snapshots) {
                for (doc in snap.documents) {
                    val lat = doc.getDouble("lat")
                    val lng = doc.getDouble("lng")

                    if (lat != null && lng != null) {
                        val docLocation = GeoLocation(lat, lng)
                        val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
                        if (distanceInM <= radius) {
                            matchingDocs.add(doc)
                        }
                    }
                }
            }

            // 3. Mapiraj spotove
            val spotDtos = matchingDocs.mapNotNull { doc ->
                try {
                    doc.toObject(FirestoreSpotDto::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            val spots = spotDtos.map { it.toDomain() }

            // 4. Fetchuj korisnike i spoji
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
                    SpotWithUser(spot)
                }
            }.sortedByDescending { it.spot.createdAt }

            Result.success(spotsWithUsers)
        } catch (e: Exception) {
            Result.failure(e)
        }

        emit(result)
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
                Result.failure(Exception("Review already exists"))
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

    suspend fun addAdditionalPhotoToSpot(spotId: String, photoUrl: String): Result<String> {
        return try {
            val spotRef = firestore.collection("spots").document(spotId)
            val spotSnapshot = spotRef.get().await()

            if (!spotSnapshot.exists()) {
                return Result.failure(Exception("Spot not found"))
            }

            // Add the new photo URL to the additionalPhotos array
            spotRef.update("additionalPhotos", FieldValue.arrayUnion(photoUrl)).await()
            Result.success(photoUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}