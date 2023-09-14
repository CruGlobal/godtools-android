plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.ui"

    configureCompose(project)

    defaultConfig.vectorDrawables.useSupportLibrary = true

    buildFeatures {
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }
}

onesky {
    sourceStringFiles = listOf(
        "strings_base_ui.xml",
    )
}

dependencies {
    api(project(":library:base"))
    implementation(project(":library:model"))

    api(libs.androidx.appcompat)
    compileOnly(libs.androidx.fragment.ktx)
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
    implementation(libs.dagger)
    implementation(libs.hilt)
    implementation(libs.youtubePlayer)

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(project(":ui:tract-renderer"))
}
