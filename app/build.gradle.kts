plugins {
    id("com.android.application")
//    id("org.jetbrains.kotlin.android")
//    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}

android {
    namespace = "ir.baran.Sticher"
    compileSdk = 34

    androidResources {
        // MediaPlayer + AssetManager.openFd() only work with uncompressed assets.
        noCompress += listOf("mp3", "wav", "ogg", "m4a")
    }

    defaultConfig {
        applicationId = "ir.baran.Sticher"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
}

dependencies {
    implementation(project(":framework"))

    implementation(project(":BaranBook"))

    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
