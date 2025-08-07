package rs.gospaleks.waterspot.domain.model

enum class SpotTypeEnum {
    FOUNTAIN,
    PUBLIC,
    REFILL_STATION,
    OTHER
}

enum class CleanlinessLevelEnum {
    CLEAN, MODERATE, DIRTY
}

data class Spot (
    val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val photoUrl: String = "",
    val type: SpotTypeEnum = SpotTypeEnum.OTHER,
    val cleanliness: CleanlinessLevelEnum = CleanlinessLevelEnum.CLEAN,
    val description: String = "",
    // userid
    // createdAt
)