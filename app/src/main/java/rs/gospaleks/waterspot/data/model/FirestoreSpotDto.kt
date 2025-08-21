package rs.gospaleks.waterspot.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import rs.gospaleks.waterspot.domain.model.SpotPhotoDomain

data class FirestoreSpotDto(
    @DocumentId val id: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val geohash: String? = null,
    val photoUrl: String? = null, // glavna fotografija (od korisnika koji je dodao spot)
    val additionalPhotos: List<SpotPhotoDomain> = emptyList(), // sada lista objekata
    val type: String = "",
    val cleanliness: String = "",
    val description: String? = null,
    val userId: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
)