plugins {
    id("com.android.dynamic-feature")
    kotlin("android")
    kotlin("kapt")
    id("org.jetbrains.kotlinx.kover")
}

android {
    namespace = "org.cru.godtools.feature.bundledcontent"
    baseConfiguration(project)
}

androidComponents.beforeVariants { it.enableUnitTest = false }
kover { disable() }

dependencies {
    implementation(project(":library:initial-content"))

    implementation(libs.gtoSupport.dagger)

    implementation(libs.dagger)

    kapt(libs.dagger.compiler)
}
