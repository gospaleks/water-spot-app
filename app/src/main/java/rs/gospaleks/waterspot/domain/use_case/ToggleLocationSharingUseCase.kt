package rs.gospaleks.waterspot.domain.use_case

import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class ToggleLocationSharingUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(isShared: Boolean): Result<Unit> {
        return userRepository.toggleLocationSharing(isShared)
    }
}