package rs.gospaleks.waterspot.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

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
    val isLocationShared: Boolean = false,
    val location: GeoPoint? = null,
    val lastUpdated: Timestamp? = null,

    // Geohash podaci
    val geohash: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)