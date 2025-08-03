package rs.gospaleks.waterspot.data.repository

import rs.gospaleks.waterspot.data.remote.firebase.FirestoreUserDataSource
import rs.gospaleks.waterspot.domain.model.User
import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestoreUserDataSource: FirestoreUserDataSource
) : UserRepository {
    override suspend fun getUserData(uid: String): Result<User> {
        return firestoreUserDataSource.getUserData(uid)
    }
}