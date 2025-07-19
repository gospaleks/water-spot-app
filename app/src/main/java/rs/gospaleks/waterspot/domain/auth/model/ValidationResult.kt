package rs.gospaleks.waterspot.domain.auth.model

data class ValidationResult(
    val successful: Boolean,
    val errorType: ValidationErrorType? = null,
)
