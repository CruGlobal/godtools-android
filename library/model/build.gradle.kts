plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

configureAndroidLibrary()

dependencies {
    implementation(project(":library:base"))

    implementation(libs.gtoSupport.jsonapi)

    testImplementation(libs.json)
}
