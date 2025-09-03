package rs.gospaleks.waterspot.domain.use_case.user

import kotlinx.coroutines.flow.Flow
import rs.gospaleks.waterspot.domain.model.User
import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class GetUserDataUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(uid: String): Flow<Result<User>> = userRepository.getUserData(uid)
}