package rs.gospaleks.waterspot.domain.auth.repository

import android.net.Uri
import rs.gospaleks.waterspot.domain.model.User

interface AuthRepository {
    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        photoUri: Uri?
    ): Result<Unit>

    suspend fun login(
        email: String,
        password: String
    ): Result<Unit>

    fun isUserLoggedIn(): Boolean

    fun logout()

    fun getCurrentUserId(): Result<String?>

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit>
}