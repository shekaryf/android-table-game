plugins {
//    alias(libs.plugins.android.application)
    alias(libs.plugins.android.library)
}

android {
    namespace = "ir.baran.baranBook"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(project(":framework"))

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.poolakey)

    // Lifecycle + LiveData + ViewModel (Java)
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.1")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.room.runtime.android)
    implementation(libs.room.common.jvm)
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    
    // ViewPager2 for tab navigation
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    
    // RecyclerView for search results
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // CardView for item layout
    implementation("androidx.cardview:cardview:1.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}
