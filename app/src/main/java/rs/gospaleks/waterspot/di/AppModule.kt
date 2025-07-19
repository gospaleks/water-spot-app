package rs.gospaleks.waterspot.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateEmailUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateLoginPasswordUseCase

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideValidateEmailUseCase(): ValidateEmailUseCase {
        return ValidateEmailUseCase()
    }

    @Provides
    fun provideValidateLoginPasswordUseCase(): ValidateLoginPasswordUseCase {
        return ValidateLoginPasswordUseCase()
    }
}