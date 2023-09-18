plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.shortcuts"

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

    ksp(libs.androidx.hilt.compiler)
    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)
}
