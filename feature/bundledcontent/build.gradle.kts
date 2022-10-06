plugins {
    id("com.android.dynamic-feature")
    kotlin("android")
    kotlin("kapt")
}

android {
    namespace = "org.cru.godtools.feature.bundledcontent"
    baseConfiguration(project)
}

dependencies {
    implementation(project(":library:initial-content"))

    implementation(libs.gtoSupport.dagger)

    implementation(libs.dagger)

    kapt(libs.dagger.compiler)
}
