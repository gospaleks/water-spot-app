package rs.gospaleks.waterspot.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import rs.gospaleks.waterspot.domain.model.User
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

    suspend fun getUserData(uid: String): Result<User> {
        try {
            val userDocRef = firestore.collection("users").document(uid)
            val userSnapshot = userDocRef.get().await()

            return if (userSnapshot.exists()) {
                val user = userSnapshot.toObject(User::class.java)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("User does not exist"))
                }
            } else {
                Result.failure(Exception("User data does not exist"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }
    }
}