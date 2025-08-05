package rs.gospaleks.waterspot.di

import android.app.Application
import com.cloudinary.android.MediaManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rs.gospaleks.waterspot.BuildConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CloudinaryModule {

    @Provides
    @Singleton
    fun provideMediaManager(application: Application): MediaManager {
        val config = hashMapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key" to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_API_SECRET,
        )
        MediaManager.init(application, config)
        return MediaManager.get()
    }
}