package rs.gospaleks.waterspot.domain.repository

import android.net.Uri
import com.firebase.geofire.GeoLocation
import kotlinx.coroutines.flow.Flow
import rs.gospaleks.waterspot.domain.model.User
import rs.gospaleks.waterspot.domain.model.UserWithSpots

interface UserRepository {
    fun getUserData(uid: String): Flow<Result<User>>

    suspend fun uploadAvatar(imageUri: Uri): Result<String>

    fun getAllUsers(): Flow<Result<List<User>>>

    fun getUserWithSpots(uid: String): Flow<Result<UserWithSpots>>

    suspend fun toggleLocationSharing(isShared: Boolean): Result<Unit>

    suspend fun setUserLocation(latitude: Double, longitude: Double): Result<Unit>

    fun getUsersWithLocationSharing(): Flow<Result<List<User>>>

    suspend fun markSpotAsVisited(spotId: String): Result<Unit>

    fun isSpotVisitedByUser(spotId: String): Flow<Result<Boolean>>

    suspend fun getVisitedSpotIds(): List<String>

    suspend fun getUsersWithLocationSharingInRadius(center: GeoLocation, radius: Double) : List<User>
}