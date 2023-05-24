plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
    alias(libs.plugins.hilt)
}

android {
    namespace = "org.cru.godtools.tutorial"

    configureCompose(project)

    defaultConfig {
        vectorDrawables.useSupportLibrary = true
    }
}

onesky {
    sourceStringFiles = listOf(
        "strings_tutorial_features.xml",
        "strings_tutorial_liveshare.xml",
        "strings_tutorial_onboarding.xml",
        "strings_tutorial_tips.xml",
    )
}

dependencies {
    implementation(project(":library:analytics"))
    implementation(project(":library:base"))
    implementation(project(":ui:base"))

    implementation(libs.godtoolsShared.analytics)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.constraintlayout.compose)

    implementation(libs.gtoSupport.androidx.compose)
    implementation(libs.gtoSupport.androidx.compose.material3)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.util)

    implementation(libs.accompanist.pager.indicators)
    implementation(libs.hilt)
    implementation(libs.lottie.compose)
    implementation(libs.splitties.intents)
    implementation(libs.youtubePlayer)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)
}
