package rs.gospaleks.waterspot.domain.use_case

import rs.gospaleks.waterspot.domain.repository.SpotRepository
import javax.inject.Inject

class GetAllSpotsUseCase @Inject constructor(
    private val spotRepository: SpotRepository
) {
    operator fun invoke() = spotRepository.getAllSpots()
}