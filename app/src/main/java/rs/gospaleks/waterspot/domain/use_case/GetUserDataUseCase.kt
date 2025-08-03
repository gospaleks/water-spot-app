package rs.gospaleks.waterspot.domain.use_case

import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class GetUserDataUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uid: String) = userRepository.getUserData(uid)
}