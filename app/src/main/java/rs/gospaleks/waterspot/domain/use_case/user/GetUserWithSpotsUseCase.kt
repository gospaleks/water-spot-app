package rs.gospaleks.waterspot.domain.use_case.user

import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class GetUserWithSpotsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(uid: String) = userRepository.getUserWithSpots(uid)
}