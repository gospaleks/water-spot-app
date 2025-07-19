package rs.gospaleks.waterspot.domain.auth.model

sealed class ValidationErrorType {
    data object EmptyEmail : ValidationErrorType()
    data object InvalidEmailFormat : ValidationErrorType()
    data object EmptyPassword : ValidationErrorType()
    data object ShortPassword : ValidationErrorType()
}