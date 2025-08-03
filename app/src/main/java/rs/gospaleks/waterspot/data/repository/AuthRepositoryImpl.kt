package rs.gospaleks.waterspot.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Delay
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import rs.gospaleks.waterspot.data.remote.firebase.FirebaseAuthDataSource
import rs.gospaleks.waterspot.data.remote.firebase.FirestoreUserDataSource
import rs.gospaleks.waterspot.domain.auth.repository.AuthRepository
import rs.gospaleks.waterspot.domain.model.User
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource
) : AuthRepository {
    override suspend fun register(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        photoUri: Uri?
    ): Result<Unit> {
        // 1. Create Firebase Auth User
        val uidResult = authDataSource.register(email, password)
        val uid = uidResult.getOrNull()

        if (uid == null) {
            return Result.failure(Exception("User creation failed"))
        }

        // 2. Upload Profile Picture if provided
        // TODO: Implement profile picture upload, firebase storage dependency is added but firebase storage requires billing to be enabled

        // 3. Save User Data to Firestore
        val userData = mapOf(
            "fullName" to fullName,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "profilePictureUrl" to (photoUri?.toString() ?: "")
        )

        val firestoreResult = firestoreUserDataSource.saveUserData(uid, userData)
        if (firestoreResult.isFailure) {
            return Result.failure(firestoreResult.exceptionOrNull() ?: Exception("Failed to save user data"))
        }

        // 4. Return success
        return Result.success(Unit)
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<Unit> {
        return authDataSource.login(email, password)
    }

    override fun isUserLoggedIn(): Boolean {
        return authDataSource.isUserLoggedIn()
    }

    override fun logout() {
        authDataSource.logout()
    }

    override fun getCurrentUserId(): Result<String?> {
        val userId = authDataSource.getCurrentUserId()
        if (userId == null) {
            return Result.failure(Exception("User not logged in"))
        }

        return Result.success(userId)
    }
}