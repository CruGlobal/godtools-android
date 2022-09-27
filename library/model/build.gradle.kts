plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    namespace = "org.cru.godtools.model"

    baseConfiguration(project)
}

dependencies {
    implementation(project(":library:base"))

    implementation(libs.gtoSupport.jsonapi)

    testImplementation(libs.json)
}
