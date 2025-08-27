package rs.gospaleks.waterspot.data.model

import com.google.firebase.firestore.DocumentId

data class FirestoreUserDto(
    // Osnovni podaci
    @DocumentId var id: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String? = "",

    // Dodatni podaci
    val points: Int = 0,

    // Ovo je za deljenje lokacije
    val locationShared: Boolean = false,

    // Geohash podaci (ako resim da pribavljam i korisnike u radius-u)
    // Za sada zbog observe pribavljam sve i vracam flow kako bih imao real-time podatke (FIX sa nekim pooling-om mozda?!?!)
    val geohash: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,

    val visitedSpots: List<String> = emptyList()
)