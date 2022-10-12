import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    alias(libs.plugins.hilt)
}

android {
    namespace = "org.cru.godtools.ui"

    baseConfiguration(project)
    configureCompose(project)

    defaultConfig.vectorDrawables.useSupportLibrary = true

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += listOf("-module-name", "base-ui")
}

dependencies {
    api(project(":library:base"))
    implementation(project(":library:model"))

    api(libs.androidx.appcompat)
    compileOnly(libs.androidx.fragment.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.compose.material3)

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

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(project(":ui:tract-renderer"))
}
