plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.base"

    configureCompose(project)
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.datastore.preferences)

    api(libs.firebase.config)

    implementation(libs.gtoSupport.androidx.core)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.kotlin.coroutines)
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
}
