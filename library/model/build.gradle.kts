plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    namespace = "org.cru.godtools.model"

    baseConfiguration(project)
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(project(":library:base"))

    implementation(libs.androidx.annotation)

    implementation(libs.gtoSupport.jsonapi)

    testImplementation(libs.json)
}
