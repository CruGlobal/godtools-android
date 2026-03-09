plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.ui"

    configureCompose(project, enableCircuit = true)

    defaultConfig.vectorDrawables.useSupportLibrary = true

    buildFeatures {
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }

    testFixtures.enable = true
}

dependencies {
    api(projects.library.base)
    implementation(projects.library.model)

    api(libs.androidx.appcompat)
    compileOnly(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.compose.material3)

    implementation(libs.firebase.dynamic.links)

    api(libs.gtoSupport.androidx.fragment)
    implementation(libs.gtoSupport.androidx.databinding)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.base)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.picasso)
    implementation(libs.gtoSupport.util)

    api(libs.eventbus)
    api(libs.materialComponents)
    implementation(libs.circuit.overlay)
    implementation(libs.circuitx.android)
    implementation(libs.circuitx.gesture.navigation)
    implementation(libs.dagger)
    implementation(libs.hilt)
    implementation(libs.youtubePlayer)

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(projects.ui.tractRenderer)

    testFixturesApi(libs.testparameterinjector)
    testFixturesImplementation(libs.circuit.overlay)
    testFixturesImplementation(libs.androidx.activity.compose)
    testFixturesImplementation(libs.androidx.compose.foundation)
    testFixturesImplementation(libs.androidx.compose.material3)
    testFixturesImplementation(libs.androidx.compose.ui)
    testFixturesImplementation(libs.androidx.lifecycle.runtime.testing)
    testFixturesImplementation(libs.junit)
    testFixturesImplementation(libs.paparazzi)
}
