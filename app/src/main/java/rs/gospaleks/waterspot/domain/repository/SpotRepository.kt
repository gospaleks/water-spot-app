package rs.gospaleks.waterspot.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import rs.gospaleks.waterspot.domain.model.Review
import rs.gospaleks.waterspot.domain.model.ReviewWithUser
import rs.gospaleks.waterspot.domain.model.Spot
import rs.gospaleks.waterspot.domain.model.SpotWithUser

interface SpotRepository {
    suspend fun addSpot(spot: Spot, photoUri: Uri): Result<Unit>

    fun getAllSpots(): Flow<Result<List<Spot>>>

    fun getAllSpotsWithUsers(): Flow<Result<List<SpotWithUser>>>

    suspend fun getSpotById(id: String): Result<Spot?>

    suspend fun addReviewToSpot(spotId: String, review: Review) : Result<Unit>

    fun getReviewsForSpot(spotId: String): Flow<Result<List<ReviewWithUser>>>

//
//
//    suspend fun updateSpot(spot: Spot): Result<Unit>
//
//    suspend fun deleteSpot(id: String): Result<Unit>
}