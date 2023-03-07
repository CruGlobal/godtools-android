plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
}

android {
    namespace = "org.cru.godtools.model"

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    testFixtures.enable = true
}

dependencies {
    implementation(project(":library:base"))

    implementation(libs.androidx.annotation)

    implementation(libs.gtoSupport.jsonapi)

    testFixturesImplementation(libs.hamcrest)

    testImplementation(libs.json)
}
