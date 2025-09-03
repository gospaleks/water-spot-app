package rs.gospaleks.waterspot.domain.use_case.review

import rs.gospaleks.waterspot.domain.model.Review
import rs.gospaleks.waterspot.domain.repository.SpotRepository
import javax.inject.Inject

class AddReviewUseCase @Inject constructor(
    private val spotRepository: SpotRepository
) {
    suspend operator fun invoke(spotId: String, review: Review) = spotRepository.addReviewToSpot(spotId, review)
}