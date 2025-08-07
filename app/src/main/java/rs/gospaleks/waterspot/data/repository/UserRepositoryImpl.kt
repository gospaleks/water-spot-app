package rs.gospaleks.waterspot.data.repository

import android.net.Uri
import rs.gospaleks.waterspot.data.remote.cloudinary.CloudinaryDataSource
import rs.gospaleks.waterspot.data.remote.firebase.FirebaseAuthDataSource
import rs.gospaleks.waterspot.data.remote.firebase.FirestoreUserDataSource
import rs.gospaleks.waterspot.domain.model.User
import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource,
    private val cloudinaryDataSource: CloudinaryDataSource
) : UserRepository {
    override suspend fun getUserData(uid: String): Result<User> {
        return firestoreUserDataSource.getUserData(uid)
    }

    override suspend fun uploadAvatar(imageUri: Uri): Result<String> {
        return try {
            val currentUserId = firebaseAuthDataSource.getCurrentUserId() ?: return Result.failure(
                Exception("No authenticated user found")
            )

            val url = cloudinaryDataSource.uploadAvatar(imageUri, currentUserId)
                ?: return Result.failure(Exception("Failed to upload avatar"))

            firestoreUserDataSource.updateUserProfilePicture(currentUserId, url)
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}