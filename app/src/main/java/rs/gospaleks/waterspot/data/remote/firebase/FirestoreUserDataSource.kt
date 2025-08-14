package rs.gospaleks.waterspot.data.remote.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import rs.gospaleks.waterspot.data.mapper.toDomain
import rs.gospaleks.waterspot.data.model.FirestoreUserDto
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
        return try {
            val userDocRef = firestore.collection("users").document(uid)
            val userSnapshot = userDocRef.get().await()

            if (userSnapshot.exists()) {
                val dto = userSnapshot.toObject(FirestoreUserDto::class.java)
                if (dto != null) {
                    Result.success(dto.toDomain())
                } else {
                    Result.failure(Exception("User data is null"))
                }
            } else {
                Result.failure(Exception("User document does not exist"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
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
}