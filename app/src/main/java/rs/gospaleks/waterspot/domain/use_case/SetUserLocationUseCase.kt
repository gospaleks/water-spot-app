package rs.gospaleks.waterspot.domain.use_case

import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class SetUserLocationUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(latitude: Double, longitude: Double): Result<Unit> {
        return userRepository.setUserLocation(latitude, longitude)
    }
}