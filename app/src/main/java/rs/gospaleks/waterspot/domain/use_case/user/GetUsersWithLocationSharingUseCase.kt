package rs.gospaleks.waterspot.domain.use_case.user

import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class GetUsersWithLocationSharingUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke() = userRepository.getUsersWithLocationSharing()
}