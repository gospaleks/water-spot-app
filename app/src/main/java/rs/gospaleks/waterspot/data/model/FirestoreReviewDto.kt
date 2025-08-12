package rs.gospaleks.waterspot.data.model

import com.google.firebase.Timestamp

data class FirestoreReviewDto(
    val userId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Timestamp = Timestamp.now(),
)