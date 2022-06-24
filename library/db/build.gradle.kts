plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android.baseConfiguration(project)

dependencies {
    api(project(":library:base"))
    implementation(project(":library:model"))

    implementation(libs.androidx.lifecycle.livedata.ktx)

    api(libs.gtoSupport.db)
    api(libs.gtoSupport.db.coroutines)
    api(libs.gtoSupport.db.livedata)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.base)
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
    implementation(libs.hilt)

    kapt(libs.dagger.compiler)

    testImplementation(libs.kotlin.coroutines.test)
}
