package rs.gospaleks.waterspot.domain.auth.use_case

import rs.gospaleks.waterspot.domain.auth.model.ValidationErrorType
import rs.gospaleks.waterspot.domain.auth.model.ValidationResult

class ValidateEmailUseCase {
    operator fun invoke(email: String) : ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(
                successful = false,
                errorType = ValidationErrorType.EmptyEmail
            )
        }

        val emailRegex = Regex(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        )
        if (!emailRegex.matches(email)) {
            return ValidationResult(
                successful = false,
                errorType = ValidationErrorType.InvalidEmailFormat
            )
        }

        return ValidationResult(successful = true)
    }
}