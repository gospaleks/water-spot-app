package rs.gospaleks.waterspot.data.mapper

import rs.gospaleks.waterspot.data.model.FirestoreUserDto
import rs.gospaleks.waterspot.domain.model.User

fun FirestoreUserDto.toDomain(): User {
    return User(
        id = id,
        email = email,
        fullName = fullName,
        phoneNumber = phoneNumber,
        profilePictureUrl = profilePictureUrl,
        points = points,
        lat = lat ?: 44.7866,
        lng = lng ?: 20.4489,
        isLocationShared = locationShared,
    )
}