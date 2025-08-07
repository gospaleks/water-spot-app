package rs.gospaleks.waterspot.data.model

import com.google.firebase.firestore.DocumentId


data class FirestoreSpotDto(
    @DocumentId val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val photoUrl: String = "",
    val type: String = "",
    val cleanliness: String = "",
    val description: String = "",
    val userId: String = "",
)
