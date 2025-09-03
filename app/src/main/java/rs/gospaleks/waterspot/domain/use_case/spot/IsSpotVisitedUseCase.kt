package rs.gospaleks.waterspot.domain.use_case.spot

import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class IsSpotVisitedUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(spotId: String) = userRepository.isSpotVisitedByUser(spotId)
}