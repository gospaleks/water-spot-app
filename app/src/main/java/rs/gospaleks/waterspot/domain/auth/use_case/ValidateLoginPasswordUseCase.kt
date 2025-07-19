package rs.gospaleks.waterspot.domain.auth.use_case

import rs.gospaleks.waterspot.domain.auth.model.ValidationErrorType
import rs.gospaleks.waterspot.domain.auth.model.ValidationResult

class ValidateLoginPasswordUseCase {
    operator fun invoke(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult(
                successful = false,
                errorType = ValidationErrorType.EmptyPassword
            )
        }

        return ValidationResult(successful = true, errorType = null)
    }
}