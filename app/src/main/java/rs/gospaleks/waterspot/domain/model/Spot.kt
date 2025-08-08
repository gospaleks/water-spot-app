package rs.gospaleks.waterspot.domain.model

import rs.gospaleks.waterspot.R

enum class SpotTypeEnum {
    FOUNTAIN,
    PUBLIC,
    REFILL_STATION,
    OTHER
}

fun SpotTypeEnum.toStringResId(): Int {
    return when (this) {
        SpotTypeEnum.FOUNTAIN -> R.string.add_spot_details_type_fountain
        SpotTypeEnum.PUBLIC -> R.string.add_spot_details_type_public
        SpotTypeEnum.REFILL_STATION -> R.string.add_spot_details_type_refill_station
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
    val type: SpotTypeEnum,
    val cleanliness: CleanlinessLevelEnum,
    val description: String?,
    val userId: String,
    val createdAt: Long?, // UNIX timestamp (millis)
    val updatedAt: Long?
)

// Spot details sluzi za prikazivanje bottom sheet modal-a sa detaljima
data class SpotDetails(
    val spot: Spot,
    val user: User? = null,
    // Additional details like user reviews, ratings, comments etc. can be added here
)