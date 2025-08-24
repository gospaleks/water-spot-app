package rs.gospaleks.waterspot.data.remote.firebase

import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import rs.gospaleks.waterspot.data.mapper.toDomain
import rs.gospaleks.waterspot.data.model.FirestoreUserDto
import rs.gospaleks.waterspot.data.model.FirestoreSpotDto
import rs.gospaleks.waterspot.domain.model.User
import rs.gospaleks.waterspot.domain.model.Spot
import rs.gospaleks.waterspot.domain.model.UserWithSpots
import javax.inject.Inject

class FirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveUserData(uid: String, userData: Map<String, String?>) : Result<Unit> {
        try {
            firestore.collection("users").document(uid).set(userData).await()
            return Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    fun getUserData(uid: String): Flow<Result<User>> = callbackFlow {
        val userDocRef = firestore.collection("users").document(uid)
        val listener = userDocRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(Result.failure(e))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                try {
                    val dto = snapshot.toObject(FirestoreUserDto::class.java)
                    val user = dto?.copy(id = snapshot.id)?.toDomain()
                    if (user != null) {
                        trySend(Result.success(user))
                    } else {
                        trySend(Result.failure(Exception("User data is null")))
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    trySend(Result.failure(ex))
                }
            } else {
                trySend(Result.failure(Exception("User document does not exist")))
            }
        }

        awaitClose { listener.remove() }
    }

    suspend fun updateUserProfilePicture(uid: String, profilePicture: String) : Result<Unit> {
        return try {
            val userDocRef = firestore.collection("users").document(uid)
            userDocRef.update("profilePictureUrl", profilePicture).await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun addPoints(uid: String, points: Int): Result<Unit> {
        return try {
            val userDocRef = firestore.collection("users").document(uid)
            userDocRef.update("points", FieldValue.increment(points.toLong())).await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun getAllUsers(): Flow<Result<List<User>>> = callbackFlow {
        val userCollectionRef = firestore.collection("users")
        val snapshotListener = userCollectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(Result.failure(e))
                return@addSnapshotListener
            }

            val users = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(FirestoreUserDto::class.java)
                        ?.copy(id = doc.id)
                        ?.toDomain()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            } ?: emptyList()

            trySend(Result.success(users))
        }

        awaitClose { snapshotListener.remove() }
    }

    fun getUserWithSpots(uid: String): Flow<Result<UserWithSpots>> = callbackFlow {
        val userDocRef = firestore.collection("users").document(uid)
        val spotsQueryRef = firestore.collection("spots").whereEqualTo("userId", uid)

        var currentUser: User? = null
        var currentSpots: List<Spot>? = null

        fun emitIfReady() {
            val u = currentUser
            val s = currentSpots
            if (u != null && s != null) {
                trySend(Result.success(UserWithSpots(u, s)))
            }
        }

        val userListener = userDocRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(Result.failure(e))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                try {
                    val dto = snapshot.toObject(FirestoreUserDto::class.java)
                    val user = dto?.copy(id = snapshot.id)?.toDomain()
                    if (user != null) {
                        currentUser = user
                        emitIfReady()
                    } else {
                        trySend(Result.failure(Exception("User data is null")))
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    trySend(Result.failure(ex))
                }
            } else {
                trySend(Result.failure(Exception("User document does not exist")))
            }
        }

        val spotsListener = spotsQueryRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(Result.failure(e))
                return@addSnapshotListener
            }

            try {
                val spots = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(FirestoreSpotDto::class.java)
                            ?.copy(id = doc.id)
                            ?.toDomain()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        null
                    }
                } ?: emptyList()

                currentSpots = spots
                emitIfReady()
            } catch (ex: Exception) {
                ex.printStackTrace()
                trySend(Result.failure(ex))
            }
        }

        awaitClose {
            userListener.remove()
            spotsListener.remove()
        }
    }

    suspend fun toggleLocationSharing(uid: String, isShared: Boolean): Result<Unit> {
        return try {
            val userDocRef = firestore.collection("users").document(uid)
            userDocRef.update("isLocationShared", isShared).await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun setUserLocation(uid: String, latitude: Double, longitude: Double, geohash: String): Result<Unit> {
        return try {
            val userDocRef = firestore.collection("users").document(uid)
            userDocRef.update(
                mapOf(
                    "lat" to latitude,
                    "lng" to longitude,
                    "geohash" to geohash
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun getUsersWithLocationSharing(): Flow<Result<List<User>>> = callbackFlow {
        val userCollectionRef = firestore.collection("users")
            .whereEqualTo("isLocationShared", true)

        val snapshotListener = userCollectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(Result.failure(e))
                return@addSnapshotListener
            }

            val users = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(FirestoreUserDto::class.java)
                        ?.copy(id = doc.id)
                        ?.toDomain()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            } ?: emptyList()

            trySend(Result.success(users))
        }

        awaitClose { snapshotListener.remove() }
    }

    suspend fun getUsersWithLocationSharingInRadius(
        center: GeoLocation = GeoLocation(43.1571, 22.5840),
        radius: Double = 10_000.0 // u metrima
    ): Result<List<User>> {
        return try {
            // Koristimo GeoFireUtils da dobijemo listu geohash opsega (query bounds)
            val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radius)

            val seenIds = mutableSetOf<String>()
            val results = mutableListOf<User>()

            for (b in bounds) {
                // Napomena: Dodavanje whereEqualTo("isLocationShared", true) uz orderBy("geohash")
                // moze zahtevati kompozitni indeks. Da izbegnemo runtime gresku, filtriramo kasnije u kodu.
                val snapshot = firestore.collection("users")
                    .orderBy("geohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash)
                    .get()
                    .await()

                for (doc in snapshot.documents) {
                    val dto = try { doc.toObject(FirestoreUserDto::class.java) } catch (_: Exception) { null }
                    val lat = dto?.lat
                    val lng = dto?.lng

                    if (dto != null && dto.geohash != null && lat != null && lng != null) {
                        val docLocation = GeoLocation(lat, lng)
                        // Precizna distanca u metrima
                        val distance = GeoFireUtils.getDistanceBetween(docLocation, center)
                        if (distance <= radius) {
                            val id = doc.id
                            if (seenIds.add(id)) {
                                // Mapiranje u domain model
                                results.add(dto.copy(id = id).toDomain())
                            }
                        }
                    }
                }
            }

            Result.success(results)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun markAsVisitedSpot(uid: String, spotId: String) : Result<Unit> {
        return try {
            val userDocRef = firestore.collection("users").document(uid)
            userDocRef.update("visitedSpots", FieldValue.arrayUnion(spotId)).await()

            // Add points for visiting a spot (2 points)
            userDocRef.update("points", FieldValue.increment(2)).await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun isSpotVisitedByUser(uid: String, spotId: String): Flow<Result<Boolean>> = callbackFlow {
        val userDocRef = firestore.collection("users").document(uid)
        val listener = userDocRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(Result.failure(e))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                try {
                    val dto = snapshot.toObject(FirestoreUserDto::class.java)
                    val visitedSpots = dto?.visitedSpots ?: emptyList()
                    val isVisited = visitedSpots.contains(spotId)

                    trySend(Result.success(isVisited))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    trySend(Result.failure(ex))
                }
            } else {
                trySend(Result.failure(Exception("User document does not exist")))
            }
        }

        awaitClose { listener.remove() }
    }
}