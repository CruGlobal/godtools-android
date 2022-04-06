plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

configureAndroidLibrary()

android {
    buildFeatures.dataBinding = true
}

dependencies {
    implementation(project(":library:api"))
    implementation(project(":library:base"))
    implementation(project(":library:db"))
    implementation(project(":library:model"))

    api(libs.androidx.hilt.work)
    api(libs.androidx.work.ktx)

    implementation(libs.gtoSupport.androidx.work)
    implementation(libs.gtoSupport.base)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.kotlin.coroutines)

    implementation(libs.dagger)
    implementation(libs.eventbus)
    implementation(libs.guava)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)

    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.kotlin.coroutines.test)

    kapt(libs.androidx.hilt.compiler)
    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)
}
