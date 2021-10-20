import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dagger.hilt.android.plugin")
}

android {
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
    api(project(":library:analytics"))
    implementation(project(":library:model"))

    api(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    compileOnly(libs.androidx.fragment.ktx)

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
}
