plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.tutorial"

    configureCompose(project, enableCircuit = true)

    defaultConfig {
        vectorDrawables.useSupportLibrary = true
    }
}

tasks.withType<Test> {
    failOnNoDiscoveredTests.set(false)
}

dependencies {
    implementation(projects.library.analytics)
    implementation(projects.library.base)
    implementation(projects.ui.base)

    implementation(libs.godtoolsShared.analytics)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.constraintlayout.compose)

    implementation(libs.gtoSupport.androidx.activity)
    implementation(libs.gtoSupport.androidx.compose)
    implementation(libs.gtoSupport.androidx.compose.material3)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.util)

    implementation(libs.accompanist.pager.indicators)
    implementation(libs.circuit.overlay)
    implementation(libs.circuitx.android)
    implementation(libs.hilt)
    implementation(libs.lottie.compose)
    implementation(libs.splitties.intents)
    implementation(libs.youtubePlayer)

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)
}
