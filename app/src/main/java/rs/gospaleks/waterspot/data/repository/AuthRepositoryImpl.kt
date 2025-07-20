package rs.gospaleks.waterspot.data.repository

import kotlinx.coroutines.delay
import rs.gospaleks.waterspot.domain.auth.repository.AuthRepository

class AuthRepositoryImpl : AuthRepository {
    override suspend fun login(
        email: String,
        password: String
    ): Result<Unit> {
        // Simulate network delay
        delay(2000)
        return Result.success(Unit)
    }
}