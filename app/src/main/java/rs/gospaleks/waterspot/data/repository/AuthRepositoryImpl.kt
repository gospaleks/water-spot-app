package rs.gospaleks.waterspot.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Delay
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import rs.gospaleks.waterspot.domain.auth.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {
    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        photoUri: Uri?
    ): Result<Unit> {
        delay(2000) // Simulating network delay for registration
        return Result.success(Unit)
    }


    override suspend fun login(
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun logout() {
        firebaseAuth.signOut()
    }
}