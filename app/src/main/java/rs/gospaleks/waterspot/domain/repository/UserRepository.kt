package rs.gospaleks.waterspot.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import rs.gospaleks.waterspot.domain.model.User

interface UserRepository {
    suspend fun getUserData(uid: String): Result<User>

    suspend fun uploadAvatar(imageUri: Uri): Result<String>

    fun getAllUsers(): Flow<Result<List<User>>>
}