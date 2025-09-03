package rs.gospaleks.waterspot.domain.use_case.spot

import rs.gospaleks.waterspot.domain.repository.SpotRepository
import javax.inject.Inject

class GetSpotByIdUseCase @Inject constructor(
    private val spotRepository: SpotRepository
) {
    suspend operator fun invoke(id: String) = spotRepository.getSpotById(id)
}