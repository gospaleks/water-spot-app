package rs.gospaleks.waterspot.domain.use_case.spot

import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.domain.repository.SpotRepository
import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class GetVisitedSpotsUseCase @Inject constructor(
    private val spotRepository: SpotRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() : Result<List<SpotWithUser>> {
        val ids = userRepository.getVisitedSpotIds()
        return spotRepository.getSpotsWithUsersByIds(ids)
    }
}