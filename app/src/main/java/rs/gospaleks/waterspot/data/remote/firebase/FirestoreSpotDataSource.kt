package rs.gospaleks.waterspot.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
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
}