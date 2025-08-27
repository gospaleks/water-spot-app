package rs.gospaleks.waterspot.domain.model

import com.google.firebase.Timestamp
import rs.gospaleks.waterspot.R

enum class SpotTypeEnum {
    PUBLIC,
    SPRING,
    WELL,
    OTHER,
}

fun SpotTypeEnum.toStringResId(): Int {
    return when (this) {
        SpotTypeEnum.PUBLIC -> R.string.add_spot_details_type_public
        SpotTypeEnum.SPRING -> R.string.add_spot_details_type_spring
        SpotTypeEnum.WELL -> R.string.add_spot_details_type_well
        SpotTypeEnum.OTHER -> R.string.add_spot_details_type_other
    }
}

enum class CleanlinessLevelEnum {
    CLEAN, MODERATE, DIRTY
}

fun CleanlinessLevelEnum.toStringResId(): Int {
    return when (this) {
        CleanlinessLevelEnum.CLEAN -> R.string.add_spot_details_cleanliness_clean
        CleanlinessLevelEnum.MODERATE -> R.string.add_spot_details_cleanliness_moderate
        CleanlinessLevelEnum.DIRTY -> R.string.add_spot_details_cleanliness_dirty
    }
}

data class Spot(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val photoUrl: String? = null,
    val additionalPhotos: List<SpotPhotoDomain> = emptyList(), // sada lista objekata
    val type: SpotTypeEnum,
    val cleanliness: CleanlinessLevelEnum,
    val description: String?,
    val userId: String,
    val createdAt: Long?, // UNIX timestamp (millis)
    val updatedAt: Long?,
    val averageRating: Double = 0.0, // Average rating from reviews
    val reviewCount: Int = 0 // Number of reviews
)

data class SpotPhotoDomain(
    val url: String = "",
    val userId: String = "",
    val addedAt: Timestamp? = null
)

data class SpotWithUser (
    val spot: Spot,
    val user: User? = null // User who created the spot
)