package rs.gospaleks.waterspot.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import rs.gospaleks.waterspot.domain.model.User
import rs.gospaleks.waterspot.domain.model.UserWithSpots

interface UserRepository {
    fun getUserData(uid: String): Flow<Result<User>>

    suspend fun uploadAvatar(imageUri: Uri): Result<String>

    fun getAllUsers(): Flow<Result<List<User>>>

    fun getUserWithSpots(uid: String): Flow<Result<UserWithSpots>>
}