plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.ksp)
}

android.namespace = "org.cru.godtools.account"

dependencies {
    implementation(project(":library:api"))

    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.facebook)
    implementation(libs.gtoSupport.kotlin.coroutines)

    implementation(libs.dagger)
    implementation(libs.hilt)

    // region Facebook
    implementation(libs.facebook.login)
    // endregion Facebook

    // region Google
    implementation(libs.gtoSupport.play.auth)
    implementation(libs.kotlin.coroutines.play.services)
    implementation(libs.play.auth)
    // endregion Google

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
}
