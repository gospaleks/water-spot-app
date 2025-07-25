package rs.gospaleks.waterspot.domain.auth.use_case

import rs.gospaleks.waterspot.domain.auth.repository.AuthRepository
import javax.inject.Inject

class IsUserLoggedInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}