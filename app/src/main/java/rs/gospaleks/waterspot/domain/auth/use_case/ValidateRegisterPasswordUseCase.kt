package rs.gospaleks.waterspot.domain.auth.use_case

import rs.gospaleks.waterspot.domain.auth.model.ValidationErrorType
import rs.gospaleks.waterspot.domain.auth.model.ValidationResult

class ValidateRegisterPasswordUseCase {
    operator fun invoke(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult(
                successful = false,
                errorType = ValidationErrorType.EmptyPassword
            )
        }

        // Minimum length of 6 characters
        if (password.length < 6) {
            return ValidationResult(
                successful = false,
                errorType = ValidationErrorType.ShortPassword
            )
        }

        // Mora da sadrzi barem jedno veliko slovo, jedno malo slovo i jedan broj
        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")
        if (!passwordRegex.matches(password)) {
            return ValidationResult(
                successful = false,
                errorType = ValidationErrorType.WeakPassword
            )
        }

        return ValidationResult(successful = true)
    }
}