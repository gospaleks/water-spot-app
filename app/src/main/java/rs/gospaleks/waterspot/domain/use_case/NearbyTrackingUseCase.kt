package rs.gospaleks.waterspot.domain.use_case

import rs.gospaleks.waterspot.domain.repository.SpotRepository
import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class NearbyTrackingUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val spotRepository: SpotRepository
) {
}