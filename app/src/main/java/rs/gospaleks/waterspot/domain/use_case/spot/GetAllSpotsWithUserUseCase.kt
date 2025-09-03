package rs.gospaleks.waterspot.domain.use_case.spot

import com.firebase.geofire.GeoLocation
import kotlinx.coroutines.flow.Flow
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.domain.repository.SpotRepository
import javax.inject.Inject

class GetAllSpotsWithUserUseCase @Inject constructor(
    private val spotRepository: SpotRepository
) {
    // default radius is 30 km
    operator fun invoke(lat: Double, lng: Double, radius: Double = 30_000.0) : Flow<Result<List<SpotWithUser>>> {
        val center = GeoLocation(lat, lng)
        return spotRepository.getAllSpotsWithUsers(center, radius)
    }
}