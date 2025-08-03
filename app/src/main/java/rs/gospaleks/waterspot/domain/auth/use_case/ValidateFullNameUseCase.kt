package rs.gospaleks.waterspot.domain.auth.use_case

import rs.gospaleks.waterspot.domain.auth.model.ValidationErrorType
import rs.gospaleks.waterspot.domain.auth.model.ValidationResult

class ValidateFullNameUseCase {
    operator fun invoke(fullName: String): ValidationResult {
        if (fullName.isBlank()) {
            return ValidationResult(
                successful = false,
                errorType = ValidationErrorType.EmptyFullName
            )
        }

        return ValidationResult(successful = true, errorType = null)
    }
}