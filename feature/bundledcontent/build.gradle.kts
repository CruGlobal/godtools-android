plugins {
    id("com.android.dynamic-feature")
    kotlin("android")
    kotlin("kapt")
}

android {
    baseConfiguration(project)
}

dependencies {
    implementation(project(":library:initial-content"))

    implementation(libs.gtoSupport.dagger)

    implementation(libs.dagger)

    kapt(libs.dagger.compiler)
}
