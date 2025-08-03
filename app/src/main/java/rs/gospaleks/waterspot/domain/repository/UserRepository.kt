package rs.gospaleks.waterspot.domain.repository

import rs.gospaleks.waterspot.domain.model.User

interface UserRepository {
    suspend fun getUserData(uid: String): Result<User>
}