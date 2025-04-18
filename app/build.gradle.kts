plugins {
    // alias(libs.plugins.androidApplication) // Assuming this is correct from your libs.versions.toml
    // alias(libs.plugins.jetbrainsKotlinAndroid) // Assuming this is correct
    id("com.android.application") // Standard way if not using alias
    id("org.jetbrains.kotlin.android") // Standard way if not using alias
    kotlin("kapt") // Correctly included
    id("kotlin-parcelize")
}

android {
    namespace = "cit.edu.WildcatFreshFinds"
    // compileSdk = 35 // Using 35 is fine if you're targeting a preview SDK, otherwise 34 is the latest stable (as of early 2024/2025)
    compileSdk = 35 // Suggest using stable 34 unless you specifically need 35

    defaultConfig {
        applicationId = "cit.edu.WildcatFreshFinds"
        minSdk = 24
        // targetSdk = 35 // Should match compileSdk
        targetSdk = 35 // Match compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
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
        // sourceCompatibility = JavaVersion.VERSION_1_8 // <-- Recommend updating
        // targetCompatibility = JavaVersion.VERSION_1_8 // <-- Recommend updating
        sourceCompatibility = JavaVersion.VERSION_11 // Or JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_11 // Or JavaVersion.VERSION_17
    }
    kotlinOptions {
        // jvmTarget = "1.8" // <-- Recommend updating
        jvmTarget = "11" // Or "17" - Should match compileOptions Java version
    }
    buildFeatures {
        compose = true
        viewBinding = true // Good, needed for ViewBinding if used elsewhere
    }
    composeOptions {
        // Ensure this version is compatible with your Kotlin plugin version
        kotlinCompilerExtensionVersion = "1.5.1" // Check for updates based on Kotlin/Compose versions
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Keep your existing dependencies using libs alias if they work
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3) // Material 3
    implementation(libs.androidx.appcompat)
    implementation(libs.material) // Material 2 - You have both M2 and M3, ensure this is intended. Usually, you'd stick to one.
    implementation(libs.androidx.activity) // Base activity library
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // --- Added/Verified Dependencies ---
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    // Activity KTX (Essential for registerForActivityResult)
    // Check if libs.androidx.activity includes KTX, otherwise add explicitly:
    implementation("androidx.activity:activity-ktx:1.8.2") // Use latest version or define in libs.versions.toml

    // Lifecycle ViewModel KTX (Good practice for architecture)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation(libs.androidx.work.runtime.ktx) // Use latest version or define in libs.versions.toml

    // Room database (Your existing declarations are correct)
    val room_version = "2.6.1" // Consider defining this in libs.versions.toml
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version") // Good for coroutines support

    // Glide (For Image Loading)
    val glide_version = "4.16.0" // Consider defining this in libs.versions.toml
    implementation("com.github.bumptech.glide:glide:$glide_version")
    kapt("com.github.bumptech.glide:compiler:$glide_version")

    // --- End Added/Verified Dependencies ---


    // Testing dependencies (Keep as they are)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Other dependencies (Keep as they are)
    implementation("de.hdodenhof:circleimageview:3.1.0")
}