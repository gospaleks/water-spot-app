package rs.gospaleks.waterspot.data.remote.firebase

import android.util.Log
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
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
import rs.gospaleks.waterspot.data.model.FirestoreUserDto
import rs.gospaleks.waterspot.domain.model.ReviewWithUser
import rs.gospaleks.waterspot.domain.model.Spot
import rs.gospaleks.waterspot.domain.model.SpotPhotoDomain
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

            // 2a. Prikupi matching dokumente uz deduplikaciju (zbog preklapanja bound-ova)
            val matchingDocsById = linkedMapOf<String, DocumentSnapshot>()
            for (snap in snapshots) {
                for (doc in snap.documents) {
                    val lat = doc.getDouble("lat")
                    val lng = doc.getDouble("lng")

                    if (lat != null && lng != null) {
                        val docLocation = GeoLocation(lat, lng)
                        val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
                        if (distanceInM <= radius) {
                            // čuvamo prvi viđeni dokument za dati ID
                            matchingDocsById.putIfAbsent(doc.id, doc)
                        }
                    }
                }
            }

            // 3. Mapiraj spotove
            val spotDtos = matchingDocsById.values.mapNotNull { doc ->
                try {
                    doc.toObject(FirestoreSpotDto::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            val spots = spotDtos.map { it.toDomain() }

            // 4. Fetchuj korisnike u batch-evima i spoji
            val userIds = spots.map { it.userId }.filter { it.isNotBlank() }.toSet()
            val usersById = mutableMapOf<String, User>()
            if (userIds.isNotEmpty()) {
                val chunks = userIds.chunked(10) // Firestore 'in' operator limit
                for (chunk in chunks) {
                    try {
                        val userSnap = firestore.collection("users")
                            .whereIn(FieldPath.documentId(), chunk)
                            .get()
                            .await()

                        for (doc in userSnap.documents) {
                            val user = doc.toObject(FirestoreUserDto::class.java)
                                ?.copy(id = doc.id)
                                ?.toDomain()
                            if (user != null) {
                                usersById[doc.id] = user
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Nastavi sa ostatkom chunk-ova čak i ako jedan padne
                    }
                }
            }

            val spotsWithUsers = spots
                .map { spot -> SpotWithUser(spot, usersById[spot.userId]) }
                .sortedByDescending { it.spot.createdAt }

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

                            val user = userSnapshot.toObject(FirestoreUserDto::class.java)
                                ?.copy(id = userSnapshot.id)
                                ?.toDomain()

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
                // Allow overwrite only if the existing review is older than one month
                val existingCreatedAt = existingReview.getDate("createdAt")
                val oneMonthAgo = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.MONTH, -1)
                }.time
                val isOlderThanMonth = existingCreatedAt?.before(oneMonthAgo) == true
                if (!isOlderThanMonth) {
                    return Result.failure(Exception("Review already exists for this month"))
                }
            }

            // Either no existing review or it's older than a month -> overwrite with the new review
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

            // Na osnovu nove prosecne ocene, azuriraj cleanlinesLevel (dinamicki se menja na osnovu ocena korisnika kako ne bi ostala zastarela informacija)
            val spotSnapshot = spotRef.get().await()
            val averageRating = spotSnapshot.getDouble("averageRating") ?: 0.0
            val cleanlinessLevel = when {
                averageRating >= 4.0 -> "CLEAN"
                averageRating >= 2.5 -> "MODERATE"
                else -> "DIRTY"
            }

            spotRef.update("cleanliness", cleanlinessLevel).await()

            // Update updatedAt field on spot
            spotRef.update("updatedAt", FieldValue.serverTimestamp()).await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun isAlreadyUploadedPhotoForSpot(spotId: String, userId: String): Result<Boolean> {
        return try {
            val spotRef = firestore.collection("spots").document(spotId)
            val spotSnapshot = spotRef.get().await()

            if (!spotSnapshot.exists()) {
                return Result.failure(Exception("Spot not found"))
            }

            val spotDto = spotSnapshot.toObject(FirestoreSpotDto::class.java)
            val oneMonthAgo = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.MONTH, -1)
            }.time

            // Return true only if the user has a photo added within the last month
            val hasRecentPhoto = spotDto?.additionalPhotos
                ?.filter { it.userId == userId }
                ?.any { photo ->
                    val date = photo.addedAt?.toDate()
                    date != null && !date.before(oneMonthAgo)
                } ?: false

            Result.success(hasRecentPhoto)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun addAdditionalPhotoToSpot(spotId: String, spotPhoto: SpotPhotoDomain): Result<SpotPhotoDomain> {
        return try {
            val spotRef = firestore.collection("spots").document(spotId)
            val spotSnapshot = spotRef.get().await()

            if (!spotSnapshot.exists()) {
                return Result.failure(Exception("Spot not found"))
            }

            // Add the new SpotPhoto object to the additionalPhotos array
            spotRef.update("additionalPhotos", FieldValue.arrayUnion(spotPhoto)).await()

            // Update updatedAt field on spot
            spotRef.update("updatedAt", FieldValue.serverTimestamp()).await()

            Result.success(spotPhoto)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getSpotsWithUsersByIds(spotIds: List<String>) : Result<List<SpotWithUser>> {
        return try {
            if (spotIds.isEmpty()) return Result.success(emptyList())

            val spotsSnap = firestore.collection("spots")
                .whereIn(FieldPath.documentId(), spotIds)
                .get()
                .await()

            val spotDtos = spotsSnap.documents.mapNotNull { doc ->
                try {
                    doc.toObject(FirestoreSpotDto::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            val spots = spotDtos.map { it.toDomain() }
            val userIds = spots.map { it.userId }.filter { it.isNotBlank() }.toSet()
            val usersById = mutableMapOf<String, User>()

            if (userIds.isNotEmpty()) {
                val chunks = userIds.chunked(10)
                for (chunk in chunks) {
                    try {
                        val userSnap = firestore.collection("users")
                            .whereIn(FieldPath.documentId(), chunk)
                            .get()
                            .await()
                        for (doc in userSnap.documents) {
                            val user = doc.toObject(FirestoreUserDto::class.java)
                                ?.copy(id = doc.id)
                                ?.toDomain()
                            if (user != null) {
                                usersById[doc.id] = user
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val spotsWithUsers = spots.map { spot -> SpotWithUser(spot, usersById[spot.userId]) }
            Result.success(spotsWithUsers)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}