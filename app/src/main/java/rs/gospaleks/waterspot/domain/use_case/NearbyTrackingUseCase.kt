package rs.gospaleks.waterspot.domain.use_case

import com.firebase.geofire.GeoLocation
import kotlinx.coroutines.flow.first
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.domain.repository.SpotRepository
import javax.inject.Inject

class NearbyTrackingUseCase @Inject constructor(
    private val spotRepository: SpotRepository
) {
    suspend operator fun invoke(
        lat: Double,
        long: Double,
        radiusMeters: Double = 100.0
    ): Result<List<SpotWithUser>> {
        // Query nearby spots-with-users within the given radius (in meters)
        val center = GeoLocation(lat, long)
        return spotRepository.getAllSpotsWithUsers(center, radiusMeters).first()
    }
}