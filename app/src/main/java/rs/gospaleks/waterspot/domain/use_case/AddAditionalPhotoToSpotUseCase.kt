package rs.gospaleks.waterspot.domain.use_case

import android.net.Uri
import rs.gospaleks.waterspot.domain.repository.SpotRepository
import javax.inject.Inject

class AddAditionalPhotoToSpotUseCase @Inject constructor(
    private val spotRepository: SpotRepository
) {
    suspend operator fun invoke(spotId: String, photoUri: Uri): Result<String> {
        return spotRepository.uploadAdditionalPhoto(spotId, photoUri)
    }
}