package rs.gospaleks.waterspot.data.repository

import android.net.Uri
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import kotlinx.coroutines.flow.Flow
import rs.gospaleks.waterspot.data.mapper.toFirestoreMap
import rs.gospaleks.waterspot.data.remote.cloudinary.CloudinaryDataSource
import rs.gospaleks.waterspot.data.remote.firebase.FirebaseAuthDataSource
import rs.gospaleks.waterspot.data.remote.firebase.FirestoreSpotDataSource
import rs.gospaleks.waterspot.data.remote.firebase.FirestoreUserDataSource
import rs.gospaleks.waterspot.domain.model.Review
import rs.gospaleks.waterspot.domain.model.ReviewWithUser
import rs.gospaleks.waterspot.domain.model.Spot
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.domain.repository.SpotRepository
import javax.inject.Inject

class SpotRepositoryImpl @Inject constructor(
    private val cloudinaryDataSource: CloudinaryDataSource,
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firestoreSpotDataSource: FirestoreSpotDataSource,
    private val firestoreUserDataSource: FirestoreUserDataSource
): SpotRepository {
    override suspend fun addSpot(spot: Spot, photoUri: Uri): Result<Unit> {
        // 1. Upload photo to cloudinary
        val url = cloudinaryDataSource.uploadSpotImage(photoUri)
            ?: return Result.failure(Exception("Failed to upload photo"))

        // 2. Create a new Spot object with the photo URL and convert it to Firestore DTO
        val updatedSpot = spot.copy(photoUrl = url)
        val spotDataMap = updatedSpot.toFirestoreMap().toMutableMap()

        // 3. Get hash for geolocation and update map
        val hash = GeoFireUtils.getGeoHashForLocation(GeoLocation(updatedSpot.latitude, updatedSpot.longitude))
        spotDataMap["geohash"] = hash

        // 4. Add points to user for adding a new spot (gamification logic)
        firestoreUserDataSource.addPoints(updatedSpot.userId, 10)

        // 5. Save spot to Firestore
        return firestoreSpotDataSource.saveSpot(spotDataMap)
    }

    override fun getAllSpots(): Flow<Result<List<Spot>>> {
        return firestoreSpotDataSource.getAllSpots()
    }

    override fun getAllSpotsWithUsers(
        center: GeoLocation,
        radius: Double
    ): Flow<Result<List<SpotWithUser>>> {
        return firestoreSpotDataSource.getAllSpotsWithUsers(center, radius)
    }

    override suspend fun getSpotById(id: String): Result<Spot?> {
        return firestoreSpotDataSource.getSpotById(id)
    }

    override suspend fun addReviewToSpot(spotId: String, review: Review): Result<Unit> {
        val reviewData = review.toFirestoreMap()
        val reviewResult = firestoreSpotDataSource.addReviewToSpot(spotId, reviewData)
        if (reviewResult.isFailure) {
            return Result.failure(reviewResult.exceptionOrNull() ?: Exception("Failed to add review"))
        }
        // Add Points to user for adding a review only if review is added (gamification logic)
        return firestoreUserDataSource.addPoints(review.userId, 5)
    }

    override fun getReviewsForSpot(spotId: String): Flow<Result<List<ReviewWithUser>>> {
        return firestoreSpotDataSource.getAllReviewsForSpotWithUsers(spotId)
    }

    override suspend fun uploadAdditionalPhoto(
        spotId: String,
        photoUri: Uri
    ): Result<String> {
        // 1. Get current user (don't proceed if not authenticated)
        val currentUserIdResult = firebaseAuthDataSource.getCurrentUserId()
        val uid = currentUserIdResult ?: return Result.failure(Exception("No authenticated user found"))

        // 2. Upload the photo to Cloudinary
        val url = cloudinaryDataSource.uploadSpotImage(photoUri)
            ?: return Result.failure(Exception("Failed to upload additional photo"))

        // 3. Add the photo URL to the spot's additional photos in Firestore
        val saveUrlResult = firestoreSpotDataSource.addAdditionalPhotoToSpot(spotId, url)
        if (saveUrlResult.isFailure) {
            return Result.failure(saveUrlResult.exceptionOrNull() ?: Exception("Failed to save additional photo URL"))
        }

        // 4. Add points to user for adding an additional photo (gamification logic)
        firestoreUserDataSource.addPoints(uid, 5)

        return Result.success(url)
    }
}