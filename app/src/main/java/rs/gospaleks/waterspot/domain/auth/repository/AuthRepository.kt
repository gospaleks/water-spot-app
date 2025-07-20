package rs.gospaleks.waterspot.domain.auth.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
}