package rs.gospaleks.waterspot.domain.auth.use_case

import rs.gospaleks.waterspot.domain.auth.repository.AuthRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        currentPassword: String,
        newPassword: String
    ) : Result<Unit> {
        return authRepository.changePassword(
            currentPassword = currentPassword,
            newPassword = newPassword
        )
    }
}