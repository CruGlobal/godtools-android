plugins {
    id("dagger.hilt.android.plugin")
}

android {
    defaultConfig {
        vectorDrawables.useSupportLibrary = true
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation(project(":library:base"))
    implementation(project(":ui:base"))

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.viewpager2)

    implementation(libs.gtoSupport.androidx.databinding)
    implementation(libs.gtoSupport.androidx.fragment)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.util)

    implementation(libs.circleindicator)
    implementation(libs.hilt)
    implementation(libs.lottie)
    implementation(libs.splitties.fragmentargs)
    implementation(libs.splitties.intents)
    implementation(libs.youtubePlayer)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)
}
