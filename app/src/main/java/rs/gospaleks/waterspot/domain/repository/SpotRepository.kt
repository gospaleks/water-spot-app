package rs.gospaleks.waterspot.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import rs.gospaleks.waterspot.domain.model.Spot

interface SpotRepository {
    suspend fun addSpot(spot: Spot, photoUri: Uri): Result<Unit>

//    suspend fun getSpotById(id: String): Result<Spot?>
//
//    fun getAllSpots(): Flow<Result<List<Spot>>>
//
//    suspend fun updateSpot(spot: Spot): Result<Unit>
//
//    suspend fun deleteSpot(id: String): Result<Unit>
}