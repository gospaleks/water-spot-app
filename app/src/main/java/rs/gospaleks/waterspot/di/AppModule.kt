package rs.gospaleks.waterspot.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rs.gospaleks.waterspot.data.repository.AuthRepositoryImpl
import rs.gospaleks.waterspot.domain.auth.repository.AuthRepository
import rs.gospaleks.waterspot.domain.auth.use_case.IsUserLoggedInUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.LoginUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.LogoutUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.RegisterUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateEmailUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateFullNameUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateLoginPasswordUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidatePhoneNumberUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateRegisterPasswordUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth)
    }

    // Auth Use Cases
    @Provides
    fun provideIsUserLoggedInUseCase(authRepository: AuthRepository): IsUserLoggedInUseCase {
        return IsUserLoggedInUseCase(authRepository)
    }

    @Provides
    fun provideRegisterUseCase(authRepository: AuthRepository): RegisterUseCase {
        return RegisterUseCase(authRepository)
    }

    @Provides
    fun provideLoginUseCase(authRepository: AuthRepository) : LoginUseCase {
        return LoginUseCase(authRepository)
    }

    @Provides
    fun provideLogoutUseCase(authRepository: AuthRepository): LogoutUseCase {
        return LogoutUseCase(authRepository)
    }

    @Provides
    fun provideValidateEmailUseCase(): ValidateEmailUseCase {
        return ValidateEmailUseCase()
    }

    @Provides
    fun provideValidateLoginPasswordUseCase(): ValidateLoginPasswordUseCase {
        return ValidateLoginPasswordUseCase()
    }

    @Provides
    fun provideValidateFullNameUseCase(): ValidateFullNameUseCase {
        return ValidateFullNameUseCase()
    }

    @Provides
    fun provideValidateRegisterPasswordUseCase(): ValidateRegisterPasswordUseCase {
        return ValidateRegisterPasswordUseCase()
    }

    @Provides
    fun provideValidatePhoneNumberUseCase(): ValidatePhoneNumberUseCase {
        return ValidatePhoneNumberUseCase()
    }
}