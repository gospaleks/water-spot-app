package rs.gospaleks.waterspot.domain.use_case

import android.net.Uri
import rs.gospaleks.waterspot.domain.model.Spot
import rs.gospaleks.waterspot.domain.repository.SpotRepository
import javax.inject.Inject

class AddSpotUseCase @Inject constructor(
    private val spotRepository: SpotRepository
) {
    suspend operator fun invoke(spot: Spot, photoUri: Uri): Result<Unit> {
        return spotRepository.addSpot(spot, photoUri)
    }
}