package rs.gospaleks.waterspot.domain.auth.model

import rs.gospaleks.waterspot.R

// TODO: Dodaj jos eror type za ostala polja u registraciji, kao sto su full name i phone number
sealed class ValidationErrorType {
    data object EmptyEmail : ValidationErrorType()
    data object InvalidEmailFormat : ValidationErrorType()
    data object EmptyPassword : ValidationErrorType()
    data object ShortPassword : ValidationErrorType()
    data object WeakPassword : ValidationErrorType()
    data object EmptyFullName : ValidationErrorType()
    data object EmptyPhoneNumber : ValidationErrorType()
    data object InvalidPhoneNumberFormat : ValidationErrorType()

}

fun getErrorMessageFromType(errorType: ValidationErrorType): Int {
    return when (errorType) {
        is ValidationErrorType.EmptyEmail -> R.string.error_empty_email
        is ValidationErrorType.InvalidEmailFormat -> R.string.error_invalid_email
        is ValidationErrorType.EmptyPassword -> R.string.error_empty_password
        is ValidationErrorType.ShortPassword -> R.string.error_short_password
        is ValidationErrorType.WeakPassword -> R.string.error_weak_password
        is ValidationErrorType.EmptyFullName -> R.string.error_empty_full_name
        is ValidationErrorType.EmptyPhoneNumber -> R.string.error_empty_phone_number
        is ValidationErrorType.InvalidPhoneNumberFormat -> R.string.error_invalid_phone_number
    }
}