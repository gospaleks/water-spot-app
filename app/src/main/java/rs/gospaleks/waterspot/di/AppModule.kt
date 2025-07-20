package rs.gospaleks.waterspot.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rs.gospaleks.waterspot.data.repository.AuthRepositoryImpl
import rs.gospaleks.waterspot.domain.auth.repository.AuthRepository
import rs.gospaleks.waterspot.domain.auth.use_case.LoginUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateEmailUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateLoginPasswordUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl()
    }

    @Provides
    fun provideLoginUseCase(authRepository: AuthRepository) : LoginUseCase {
        return LoginUseCase(authRepository)
    }

    @Provides
    fun provideValidateEmailUseCase(): ValidateEmailUseCase {
        return ValidateEmailUseCase()
    }

    @Provides
    fun provideValidateLoginPasswordUseCase(): ValidateLoginPasswordUseCase {
        return ValidateLoginPasswordUseCase()
    }
}