package rs.gospaleks.waterspot.domain.use_case

import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserDataUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(fullName: String, phoneNumber: String): Result<Unit> {
        return userRepository.updateUserData(fullName, phoneNumber)
    }
}