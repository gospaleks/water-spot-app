package rs.gospaleks.waterspot.domain.use_case

import android.net.Uri
import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class UploadAvatarUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(imageUri: Uri) = userRepository.uploadAvatar(imageUri)

}