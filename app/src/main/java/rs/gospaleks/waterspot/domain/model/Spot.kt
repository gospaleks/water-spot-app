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