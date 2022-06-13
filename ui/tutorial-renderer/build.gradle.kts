plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    alias(libs.plugins.hilt)
}

android {
    baseConfiguration(project)
    configureCompose(project)

    defaultConfig {
        vectorDrawables.useSupportLibrary = true
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation(project(":library:analytics"))
    implementation(project(":library:base"))
    implementation(project(":ui:base"))

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.compose.material3)

    implementation(libs.gtoSupport.androidx.databinding)
    implementation(libs.gtoSupport.androidx.fragment)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.util)

    implementation(libs.circleindicator)
    implementation(libs.hilt)
    implementation(libs.lottie)
    implementation(libs.lottie.compose)

    implementation(libs.splitties.fragmentargs)
    implementation(libs.splitties.intents)
    implementation(libs.youtubePlayer)
    implementation("androidx.compose.ui:ui:1.1.1")

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)
}
