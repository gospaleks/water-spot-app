package rs.gospaleks.waterspot.data.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import rs.gospaleks.waterspot.data.mapper.toFirestoreMap
import rs.gospaleks.waterspot.data.remote.cloudinary.CloudinaryDataSource
import rs.gospaleks.waterspot.data.remote.firebase.FirestoreSpotDataSource
import rs.gospaleks.waterspot.domain.model.Review
import rs.gospaleks.waterspot.domain.model.ReviewWithUser
import rs.gospaleks.waterspot.domain.model.Spot
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.domain.repository.SpotRepository
import javax.inject.Inject

class SpotRepositoryImpl @Inject constructor(
    private val cloudinaryDataSource: CloudinaryDataSource,
    private val firestoreSpotDataSource: FirestoreSpotDataSource
): SpotRepository {
    override suspend fun addSpot(spot: Spot, photoUri: Uri): Result<Unit> {
        // 1. Upload photo to cloudinary
        val url = cloudinaryDataSource.uploadSpotImage(photoUri)
            ?: return Result.failure(Exception("Failed to upload photo"))

        // 2. Create a new Spot object with the photo URL and convert it to Firestore DTO
        val updatedSpot = spot.copy(photoUrl = url)
        val spotDataMap = updatedSpot.toFirestoreMap()

        // 3. Save spot to Firestore
        return firestoreSpotDataSource.saveSpot(spotDataMap)
    }

    override fun getAllSpots(): Flow<Result<List<Spot>>> {
        return firestoreSpotDataSource.getAllSpots()
    }

    override fun getAllSpotsWithUsers(): Flow<Result<List<SpotWithUser>>> {
        return firestoreSpotDataSource.getAllSpotsWithUsers()
    }

    override suspend fun getSpotById(id: String): Result<Spot?> {
        return firestoreSpotDataSource.getSpotById(id)
    }

    override suspend fun addReviewToSpot(spotId: String, review: Review) : Result<Unit> {
        val reviewData = review.toFirestoreMap()
        return firestoreSpotDataSource.addReviewToSpot(spotId, reviewData)
    }

    override fun getReviewsForSpot(spotId: String): Flow<Result<List<ReviewWithUser>>> {
        return firestoreSpotDataSource.getAllReviewsForSpotWithUsers(spotId)
    }
}