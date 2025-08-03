package rs.gospaleks.waterspot.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveUserData(uid: String, userData: Map<String, Any>) : Result<Unit> {
        try {
            firestore.collection("users").document(uid).set(userData).await()
            return Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }
    }
}