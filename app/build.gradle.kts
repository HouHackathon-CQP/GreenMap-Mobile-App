plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.houhackathon.greenmap_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.houhackathon.greenmap_app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load API key from local.properties
        val properties = org.jetbrains.kotlin.konan.properties.Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        buildConfigField(
            "String",
            "MAPTILER_API_KEY",
            "\"${properties.getProperty("MAPTILER_API_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"${properties.getProperty("API_BASE_URL", "")}\""
        )

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Compose
    implementation(libs.bundles.compose)

    //  Networking (Retrofit, OkHttp...)
    implementation(libs.bundles.networking)

    // Gọi bundle Room
    implementation(libs.bundles.room)

    // --- HILT & KSP  ---
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)

    // Room Compiler
    ksp(libs.androidx.room.compiler)

    // --- MAP LIBRE ---
    implementation(libs.maplibre.android.sdk)

    // ---Nav 3 ---
    implementation(libs.bundles.nav3)


    // --- Utils ---
    implementation(libs.timber)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
