plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    namespace = "org.cru.godtools.user"

    baseConfiguration(project)
}

dependencies {
    implementation(project(":library:db"))
    implementation(project(":library:model"))
    implementation(project(":library:sync"))

    implementation(libs.gtoSupport.dagger)

    implementation(libs.dagger)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)
}
