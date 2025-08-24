package rs.gospaleks.waterspot.data.repository

import android.net.Uri
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import kotlinx.coroutines.flow.Flow
import rs.gospaleks.waterspot.data.remote.cloudinary.CloudinaryDataSource
import rs.gospaleks.waterspot.data.remote.firebase.FirebaseAuthDataSource
import rs.gospaleks.waterspot.data.remote.firebase.FirestoreUserDataSource
import rs.gospaleks.waterspot.domain.model.User
import rs.gospaleks.waterspot.domain.model.UserWithSpots
import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource,
    private val cloudinaryDataSource: CloudinaryDataSource
) : UserRepository {
    override fun getUserData(uid: String): Flow<Result<User>> {
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

    override fun getAllUsers(): Flow<Result<List<User>>> {
        return firestoreUserDataSource.getAllUsers()
    }

    override fun getUserWithSpots(uid: String): Flow<Result<UserWithSpots>> {
        return firestoreUserDataSource.getUserWithSpots(uid)
    }

    override suspend fun toggleLocationSharing(
        isShared: Boolean
    ): Result<Unit> {
        val uid = firebaseAuthDataSource.getCurrentUserId()

        if (uid == null) {
            return Result.failure(Exception("No authenticated user found"))
        }

        return firestoreUserDataSource.toggleLocationSharing(uid, isShared)
    }

    override suspend fun setUserLocation(
        latitude: Double,
        longitude: Double
    ): Result<Unit> {
        val uid = firebaseAuthDataSource.getCurrentUserId()

        if (uid == null) {
            return Result.failure(Exception("No authenticated user found"))
        }

        val geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(latitude, longitude))

        return firestoreUserDataSource.setUserLocation(uid, latitude, longitude, geohash)
    }

    override fun getUsersWithLocationSharing(): Flow<Result<List<User>>> {
        return firestoreUserDataSource.getUsersWithLocationSharing()
    }

    override suspend fun markSpotAsVisited(spotId: String): Result<Unit> {
        val uid = firebaseAuthDataSource.getCurrentUserId()

        if (uid == null) {
            return Result.failure(Exception("No authenticated user found"))
        }

        return firestoreUserDataSource.markAsVisitedSpot(uid, spotId)
    }

    override fun isSpotVisitedByUser(spotId: String): Flow<Result<Boolean>> {
        val uid = firebaseAuthDataSource.getCurrentUserId()
        return if (uid != null) {
            firestoreUserDataSource.isSpotVisitedByUser(uid, spotId)
        } else {
            kotlinx.coroutines.flow.flow { emit(Result.failure(Exception("No authenticated user found"))) }
        }
    }
}