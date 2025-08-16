package rs.gospaleks.waterspot.domain.use_case

import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class GetAllUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke() = userRepository.getAllUsers()
}