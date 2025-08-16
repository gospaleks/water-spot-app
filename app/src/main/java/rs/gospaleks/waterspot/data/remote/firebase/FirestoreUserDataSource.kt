package rs.gospaleks.waterspot.data.remote.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
}