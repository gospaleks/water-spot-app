package rs.gospaleks.waterspot.data.mapper

import com.google.firebase.firestore.FieldValue
import rs.gospaleks.waterspot.data.model.FirestoreReviewDto
import rs.gospaleks.waterspot.domain.model.Review

fun FirestoreReviewDto.toDomain(): Review {
    return Review(
        userId = userId,
        rating = rating,
        comment = comment,
        createdAt = createdAt.toDate().time
    )
}

fun Review.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "userId" to userId,
        "rating" to rating,
        "comment" to comment,
        "createdAt" to FieldValue.serverTimestamp()
    )
}