package rs.gospaleks.waterspot.domain.auth.use_case

import rs.gospaleks.waterspot.domain.auth.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke() {
        return authRepository.logout()
    }
}