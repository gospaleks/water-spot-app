package rs.gospaleks.waterspot.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String? = "",
    val points: Int = 0,
    val lat: Double = 44.7866,
    val lng: Double = 20.4489
)

data class UserWithSpots (
    val user: User,
    val spots: List<Spot> = emptyList() // List of spots created by the user
)