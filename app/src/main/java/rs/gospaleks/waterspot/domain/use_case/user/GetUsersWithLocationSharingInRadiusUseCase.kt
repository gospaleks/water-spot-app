package rs.gospaleks.waterspot.domain.use_case.user

import com.firebase.geofire.GeoLocation
import rs.gospaleks.waterspot.domain.model.User
import rs.gospaleks.waterspot.domain.repository.UserRepository
import javax.inject.Inject

class GetUsersWithLocationSharingInRadiusUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(lat: Double, lng: Double, radius: Double): List<User> {
        val center = GeoLocation(lat, lng)
        return userRepository.getUsersWithLocationSharingInRadius(center, radius)
    }
}