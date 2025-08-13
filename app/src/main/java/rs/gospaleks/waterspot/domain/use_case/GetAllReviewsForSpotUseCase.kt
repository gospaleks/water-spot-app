package rs.gospaleks.waterspot.domain.use_case

import rs.gospaleks.waterspot.domain.repository.SpotRepository
import javax.inject.Inject

class GetAllReviewsForSpotUseCase @Inject constructor(
    private val spotRepository: SpotRepository
) {
    operator fun invoke(spotId: String) = spotRepository.getReviewsForSpot(spotId)
}