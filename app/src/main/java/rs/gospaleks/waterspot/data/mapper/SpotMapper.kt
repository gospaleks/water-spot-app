package rs.gospaleks.waterspot.data.mapper

import rs.gospaleks.waterspot.data.model.FirestoreSpotDto
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum
import rs.gospaleks.waterspot.domain.model.Spot
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum

fun String.toSpotTypeEnum(): SpotTypeEnum = when (this.uppercase()) {
    "FOUNTAIN" -> SpotTypeEnum.FOUNTAIN
    "PUBLIC" -> SpotTypeEnum.PUBLIC
    "REFILL_STATION" -> SpotTypeEnum.REFILL_STATION
    else -> SpotTypeEnum.OTHER
}

fun SpotTypeEnum.toFirestoreString(): String = this.name

fun String.toCleanlinessLevelEnum(): CleanlinessLevelEnum = when (this.uppercase()) {
    "CLEAN" -> CleanlinessLevelEnum.CLEAN
    "MODERATE" -> CleanlinessLevelEnum.MODERATE
    "DIRTY" -> CleanlinessLevelEnum.DIRTY
    else -> CleanlinessLevelEnum.CLEAN
}

fun CleanlinessLevelEnum.to(): String = this.name

fun FirestoreSpotDto.toDomain(id: String): Spot {
    return Spot(
        id = id,
        latitude = latitude,
        longitude = longitude,
        photoUrl = photoUrl,
        type = type.toSpotTypeEnum(),
        cleanliness = cleanliness.toCleanlinessLevelEnum(),
        description = description
    )
}
