package rs.gospaleks.waterspot.data.repository

import android.net.Uri
import rs.gospaleks.waterspot.data.mapper.toFirestoreMap
import rs.gospaleks.waterspot.data.remote.cloudinary.CloudinaryDataSource
import rs.gospaleks.waterspot.data.remote.firebase.FirestoreSpotDataSource
import rs.gospaleks.waterspot.domain.model.Spot
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
}