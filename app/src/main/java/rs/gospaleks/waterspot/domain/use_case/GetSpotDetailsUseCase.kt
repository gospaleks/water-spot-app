package rs.gospaleks.waterspot.domain.use_case

import rs.gospaleks.waterspot.domain.model.SpotDetails
import rs.gospaleks.waterspot.domain.repository.SpotRepository
import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class GetSpotDetailsUseCase @Inject constructor(
    private val spotRepository: SpotRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(spotId: String) : Result<SpotDetails> {
        val spotResult = spotRepository.getSpotById(spotId)
        val spot = spotResult.getOrNull() ?: return Result.failure(Exception("Spot not found"))

        val userResult = userRepository.getUserData(spot.userId)
        val user = userResult.getOrNull() ?: return Result.failure(Exception(userResult.exceptionOrNull()?.message ?: "User not found"))
        return Result.success(
            SpotDetails(
                spot = spot,
                user = user
            )
        )
    }
}