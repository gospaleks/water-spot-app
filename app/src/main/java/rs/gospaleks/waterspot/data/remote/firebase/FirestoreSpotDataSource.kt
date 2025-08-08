package rs.gospaleks.waterspot.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import rs.gospaleks.waterspot.data.mapper.toDomain
import rs.gospaleks.waterspot.data.model.FirestoreSpotDto
import rs.gospaleks.waterspot.domain.model.Spot
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
}