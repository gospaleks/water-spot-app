package rs.gospaleks.waterspot.data.model

import com.google.firebase.firestore.DocumentId

data class FirestoreUserDto(
    @DocumentId var id: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String? = "",
)