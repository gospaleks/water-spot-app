package rs.gospaleks.waterspot.domain.auth.use_case

import android.net.Uri
import rs.gospaleks.waterspot.domain.auth.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String,
        photoUri: Uri?
    ): Result<Unit> {
        return authRepository.register(
            fullName = fullName,
            email = email,
            password = password,
            phoneNumber = phoneNumber,
            photoUri = photoUri
        )
    }
}