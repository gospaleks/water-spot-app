package rs.gospaleks.waterspot.data.repository

import android.net.Uri
import rs.gospaleks.waterspot.data.remote.cloudinary.CloudinaryDataSource
import rs.gospaleks.waterspot.data.remote.firebase.FirebaseAuthDataSource
import rs.gospaleks.waterspot.data.remote.firebase.FirestoreUserDataSource
import rs.gospaleks.waterspot.domain.auth.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource,
    private val cloudinaryDataSource: CloudinaryDataSource
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

        // 2. Upload Profile Picture to Cloudinary
        var photoUrl: String? = null;
        if (photoUri != null) {
            photoUrl = cloudinaryDataSource.uploadAvatar(photoUri, uid)
        }

        // 3. Save User Data to Firestore
        val userData = mapOf(
            "fullName" to fullName,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "profilePictureUrl" to photoUrl
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