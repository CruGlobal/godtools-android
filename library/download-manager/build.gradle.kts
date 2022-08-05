plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    baseConfiguration(project)

    buildFeatures.dataBinding = true
}

dependencies {
    implementation(project(":library:api"))
    implementation(project(":library:base"))
    implementation(project(":library:db"))
    implementation(project(":library:model"))

    api(libs.androidx.hilt.work)
    api(libs.androidx.lifecycle.livedata.ktx)
    api(libs.androidx.work.ktx)

    implementation(libs.gtoSupport.androidx.work)
    implementation(libs.gtoSupport.base)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.kotlin.coroutines)

    implementation(libs.godtoolsMpp.parser)

    implementation(libs.dagger)
    implementation(libs.eventbus)
    implementation(libs.guava)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)

    kapt(libs.androidx.hilt.compiler)
    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.hilt.testing)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)

    kaptTest(libs.hilt.compiler)
}
