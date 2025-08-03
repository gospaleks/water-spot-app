package rs.gospaleks.waterspot.domain.auth.use_case

import rs.gospaleks.waterspot.domain.auth.model.ValidationErrorType
import rs.gospaleks.waterspot.domain.auth.model.ValidationResult

class ValidatePhoneNumberUseCase {
    operator fun invoke(phoneNumber: String): ValidationResult {
        if (phoneNumber.isBlank()) {
            return ValidationResult(
                successful = false,
                errorType = ValidationErrorType.EmptyPhoneNumber
            )
        }

        // Duzina 10 do 15 cifara, moze poceti sa +, ali ne mora
        // Ne sme sadrzati slova, specijalne karaktere
        // Primeri: +381601234567, 0601234567, 1234567890
        val phoneNumberRegex = Regex("^\\+?[0-9]{10,15}$")
        if (!phoneNumberRegex.matches(phoneNumber)) {
            return ValidationResult(
                successful = false,
                errorType = ValidationErrorType.InvalidPhoneNumberFormat
            )
        }

        return ValidationResult(successful = true)
    }
}