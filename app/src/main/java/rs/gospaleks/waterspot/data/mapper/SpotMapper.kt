package rs.gospaleks.waterspot.data.mapper

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import rs.gospaleks.waterspot.data.model.FirestoreSpotDto
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum
import rs.gospaleks.waterspot.domain.model.Spot
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum

fun FirestoreSpotDto.toDomain(): Spot {
    return Spot(
        id = id,
        latitude = lat,
        longitude = lng,
        photoUrl = photoUrl,
        type = type.toSpotTypeEnum(),
        cleanliness = cleanliness.toCleanlinessLevelEnum(),
        description = description,
        userId = userId,
        createdAt = createdAt?.toDate()?.time,
        updatedAt = updatedAt?.toDate()?.time,
        averageRating = averageRating,
        reviewCount = reviewCount
    )
}

fun Spot.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "lat" to latitude,
        "lng" to longitude,
        "geohash" to null, // Geohash can be generated if needed
        "photoUrl" to photoUrl,
        "type" to type.toFirestoreString(),
        "cleanliness" to cleanliness.toFirestoreString(),
        "description" to description,
        "userId" to userId,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

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

fun CleanlinessLevelEnum.toFirestoreString(): String = this.name