plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    id("kotlin-parcelize")
}

android {
    namespace = "top.checka.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "top.checka.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = findProperty("RELEASE_KEY_ALIAS") as String? ?: "key0"
            keyPassword = findProperty("RELEASE_KEY_PASSWORD") as String? ?: "android"
            storeFile = file(findProperty("RELEASE_STORE_FILE") as String? ?: "keystore.jks")
            storePassword = findProperty("RELEASE_STORE_PASSWORD") as String? ?: "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            ndk {
                debugSymbolLevel = "full"
            }
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.ui.text.google.fonts)
    debugImplementation(libs.androidx.ui.tooling)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.android.gms:play-services-games-v2:21.0.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation(libs.firebase.messaging)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}