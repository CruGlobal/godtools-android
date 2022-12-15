plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    namespace = "org.cru.godtools.base"

    baseConfiguration(project)
}

onesky {
    sourceStringFiles = listOf(
        "strings_language_names.xml",
    )
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.kotlin.coroutines)
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
}
