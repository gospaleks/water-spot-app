package rs.gospaleks.waterspot.domain.model

data class Review (
    val userId: String,
    val rating: Int, // 1 to 5
    val comment: String,
    val createdAt: Long? = null // UNIX timestamp (millis)
)