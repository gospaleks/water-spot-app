package rs.gospaleks.waterspot.di

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rs.gospaleks.waterspot.data.remote.cloudinary.CloudinaryDataSource
import rs.gospaleks.waterspot.data.remote.firebase.FirebaseAuthDataSource
import rs.gospaleks.waterspot.data.remote.firebase.FirestoreSpotDataSource
import rs.gospaleks.waterspot.data.remote.firebase.FirestoreUserDataSource
import rs.gospaleks.waterspot.data.repository.AuthRepositoryImpl
import rs.gospaleks.waterspot.data.repository.SpotRepositoryImpl
import rs.gospaleks.waterspot.data.repository.UserRepositoryImpl
import rs.gospaleks.waterspot.domain.auth.repository.AuthRepository
import rs.gospaleks.waterspot.domain.auth.use_case.GetCurrentUserUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.IsUserLoggedInUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.LoginUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.LogoutUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.RegisterUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateEmailUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateFullNameUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateLoginPasswordUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidatePhoneNumberUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateRegisterPasswordUseCase
import rs.gospaleks.waterspot.domain.repository.SpotRepository
import rs.gospaleks.waterspot.domain.repository.UserRepository
import rs.gospaleks.waterspot.domain.use_case.AddSpotUseCase
import rs.gospaleks.waterspot.domain.use_case.GetAllSpotsUseCase
import rs.gospaleks.waterspot.domain.use_case.GetAllSpotsWithUserUseCase
import rs.gospaleks.waterspot.domain.use_case.GetSpotDetailsUseCase
import rs.gospaleks.waterspot.domain.use_case.GetUserDataUseCase
import rs.gospaleks.waterspot.domain.use_case.LocationTrackingUseCase
import rs.gospaleks.waterspot.domain.use_case.UploadAvatarUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Firebase Instances
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // Data Sources
    @Provides
    @Singleton
    fun provideFirebaseAuthDataSource(firebaseAuth: FirebaseAuth): FirebaseAuthDataSource {
        return FirebaseAuthDataSource(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideFirestoreUserDataSource(firestore: FirebaseFirestore): FirestoreUserDataSource {
        return FirestoreUserDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideFirestoreSpotDataSource(firestore: FirebaseFirestore): FirestoreSpotDataSource {
        return FirestoreSpotDataSource(firestore)
    }

    // Repositories
    @Provides
    @Singleton
    fun provideAuthRepository(
        authDataSource: FirebaseAuthDataSource,
        firestoreUserDataSource: FirestoreUserDataSource,
        cloudinaryDataSource: CloudinaryDataSource
    ): AuthRepository {
        return AuthRepositoryImpl(authDataSource, firestoreUserDataSource, cloudinaryDataSource)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firebaseAuthDataSource: FirebaseAuthDataSource,
        firestoreUserDataSource: FirestoreUserDataSource,
        cloudinaryDataSource: CloudinaryDataSource,
    ): UserRepository {
        return UserRepositoryImpl(firebaseAuthDataSource, firestoreUserDataSource, cloudinaryDataSource)
    }

    @Provides
    @Singleton
    fun provideSpotRepository(cloudinaryDataSource: CloudinaryDataSource, firestoreSpotDataSource: FirestoreSpotDataSource): SpotRepository {
        return SpotRepositoryImpl(cloudinaryDataSource, firestoreSpotDataSource)
    }

    // Use Cases
    @Provides
    @Singleton
    fun provideLocationTrackingUseCase(fusedLocationClient: FusedLocationProviderClient) : LocationTrackingUseCase {
        return LocationTrackingUseCase(fusedLocationClient)
    }

    @Provides
    fun provideGetAllSpotsWithUserUseCase(spotRepository: SpotRepository) : GetAllSpotsWithUserUseCase {
        return GetAllSpotsWithUserUseCase(spotRepository)
    }

    @Provides
    fun provideGetSpotDetailsUseCase(spotRepository: SpotRepository, userRepository: UserRepository): GetSpotDetailsUseCase {
        return GetSpotDetailsUseCase(spotRepository, userRepository)
    }

    @Provides
    fun provideGetAllSpotsUseCase(spotRepository: SpotRepository) : GetAllSpotsUseCase {
        return GetAllSpotsUseCase(spotRepository)
    }

    @Provides
    fun provideUploadAvatarUseCase(userRepository: UserRepository): UploadAvatarUseCase {
        return UploadAvatarUseCase(userRepository)
    }

    @Provides
    fun provideGetUserDataUseCase(userRepository: UserRepository): GetUserDataUseCase {
        return GetUserDataUseCase(userRepository)
    }

    @Provides
    fun provideAddSpotUseCase(spotRepository: SpotRepository) : AddSpotUseCase {
        return AddSpotUseCase(spotRepository)
    }

    // Auth Use Cases
    @Provides
    fun provideGetCurrentUserUserCase(authRepository: AuthRepository): GetCurrentUserUseCase {
        return GetCurrentUserUseCase(authRepository)
    }

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