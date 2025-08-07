package rs.gospaleks.waterspot.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String? = ""
)