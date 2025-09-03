package rs.gospaleks.waterspot.domain.use_case.spot

import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class MarkSpotAsVisitedUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(spotId: String): Result<Unit> {
        return userRepository.markSpotAsVisited(spotId)
    }
}