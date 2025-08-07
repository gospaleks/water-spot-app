package rs.gospaleks.waterspot.domain.model

import com.google.firebase.firestore.DocumentId

data class User (
    @DocumentId var id: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String = "",
    // TODO: Add other fields as needed
)