plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.smartcart"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.smartcart"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        dataBinding = true

    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }


}

dependencies {
    implementation(libs.recyclerview)
    implementation(libs.material.v14)
    implementation (libs.gson)
    implementation (libs.glide)
    implementation(libs.protolite.well.known.types)
    annotationProcessor (libs.compiler)
    implementation (libs.picasso)

    implementation(libs.core.v190)  // Check for the latest version
    implementation(libs.recyclerview.v130)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.core)
    implementation(libs.appcompat)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    implementation(libs.material.v190)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.google.flexbox )


    implementation (libs.lifecycle.extensions) // For ViewModel and LiveData

    // For data binding
    implementation (libs.databinding.runtime) // Make sure this matches your AGP version



    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database)
    implementation(libs.firebase.analytics)
}
