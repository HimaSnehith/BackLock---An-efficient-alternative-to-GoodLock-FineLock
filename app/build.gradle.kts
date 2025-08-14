/*
 * This is the Gradle file for your app module. It defines the app's dependencies,
 * build configurations, and Android settings.
 */

plugins {
    // Apply the Android Application plugin to enable Android-specific build tasks.
    id("com.android.application")

    // Apply the Kotlin Android plugin to enable Kotlin features for Android.
    id("org.jetbrains.kotlin.android")

    // The Compose Compiler Gradle plugin is now required for Kotlin 2.0+
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    // Configure the Android SDK versions for your app.
    namespace = "com.dark.badlock"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dark.badlock"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Configure build types for different environments (e.g., release, debug).
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Configure compilation options.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // Configure Kotlin compilation options.
    kotlinOptions {
        jvmTarget = "1.8"
    }

    // Enable Jetpack Compose in your project.
    buildFeatures {
        compose = true
    }

    // Set up Compose compiler options.
    composeOptions {
        // Ensure this version matches the Kotlin compiler version
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    // Packaging options for the APK.
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // --- Android and Kotlin Core Dependencies ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.code.gson:gson:2.10.1")

    // --- Compose Dependencies ---
    // Core Compose libraries.
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // THIS IS THE NEW LINE ADDED TO FIX THE BUILD ERROR
    // This dependency contains a wider range of icons, including those you're using.
    implementation("androidx.compose.material:material-icons-extended")

    // The core material icons library, which you already had.
    implementation("androidx.compose.material:material-icons-core")

    // Additional foundation dependencies for compose
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.foundation:foundation-layout")

    // --- Testing Dependencies ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // --- Debugging and Tooling Dependencies ---
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
