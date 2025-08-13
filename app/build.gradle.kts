import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

val localProps = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}
val mapsApiKey: String = localProps.getProperty("MAPS_API_KEY") ?: ""

android.buildFeatures.buildConfig = true

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.google.gms.google.services)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0"
}

buildscript {
    dependencies {
        classpath(libs.secrets.gradle.plugin)
    }
}

android {
    namespace = "rs.gospaleks.waterspot"
    compileSdk = 36

    defaultConfig {
        applicationId = "rs.gospaleks.waterspot"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey

        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${localProps["CLOUDINARY_CLOUD_NAME"]}\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"${localProps["CLOUDINARY_API_KEY"]}\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"${localProps["CLOUDINARY_API_SECRET"]}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.cloudinary.android) {
        exclude(group = "com.facebook.fresco") // Exclude Fresco to avoid conflicts with Coil and 16KB paging issue
    }

    // Google Maps
    implementation(libs.maps.compose)
    implementation (libs.play.services.maps)
    implementation (libs.play.services.location)

    // Firebase Auth
    implementation(libs.firebase.auth)

    // Firebase Firestore
    implementation(libs.firebase.firestore)

    // Firebase Storage
    implementation(libs.firebase.storage)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Icons
    implementation(libs.androidx.material.icons.extended)

    // Coil for image loading
    implementation(libs.coil.compose)

    // Permission library
    implementation(libs.accompanist.permissions)

    // Datastore for preferences (theme, services, etc.)
    implementation(libs.androidx.datastore.preferences)

    // TODO: Add geofire dependency

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}