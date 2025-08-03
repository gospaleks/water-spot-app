package rs.gospaleks.waterspot.data.remote.firebase

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        email: String,
        password: String,
    ): Result<String> {
        return try {
            // Create Firebase Auth User
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            if (result.user == null) {
                return Result.failure(Exception("User creation failed"))
            }

            Result.success(result.user!!.uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}