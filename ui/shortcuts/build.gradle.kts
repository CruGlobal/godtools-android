plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    alias(libs.plugins.hilt)
}

android {
    baseConfiguration(project)
    createEventBusIndex("org.cru.godtools.shortcuts.ShortcutsEventBusIndex")
}

dependencies {
    implementation(project(":library:base"))
    implementation(project(":library:db"))
    implementation(project(":library:model"))

    implementation(project(":ui:base"))
    implementation(project(":ui:base-tool"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.hilt.work)
    api(libs.androidx.work.ktx)

    implementation(libs.firebase.perf.ktx)

    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.picasso)
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
    implementation(libs.hilt)
    implementation(libs.picasso)

    testImplementation(libs.gtoSupport.testing.timber)
    testImplementation(libs.kotlin.coroutines.test)

    kapt(libs.androidx.hilt.compiler)
    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)
    kapt(libs.eventbus.annotationProcessor)
}
