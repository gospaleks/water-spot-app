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

    // Ovo je za deljenje lokacije
    val isLocationShared: Boolean = false,
    val location: GeoPoint? = null,
    val lastUpdated: Timestamp? = null,
)