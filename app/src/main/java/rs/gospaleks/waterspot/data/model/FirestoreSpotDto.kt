package rs.gospaleks.waterspot.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class FirestoreSpotDto(
    @DocumentId val id: String = "",
    val location: GeoPoint? = null,
    val photoUrl: String? = null,
    val type: String = "",
    val cleanliness: String = "",
    val description: String? = null,
    val userId: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
)