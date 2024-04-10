plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.account"

    configureCompose(project)
}

dependencies {
    implementation(project(":library:api"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.gtoSupport.androidx.activity)
    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.kotlin.coroutines)

    implementation(libs.dagger)
    implementation(libs.hilt)

    // region Facebook
    implementation(libs.gtoSupport.facebook)
    implementation(libs.facebook.login)
    // endregion Facebook

    // region Google
    implementation(libs.gtoSupport.play.auth)
    implementation(libs.kotlin.coroutines.play.services)
    implementation(libs.play.auth)
    // endregion Google

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(libs.json)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
}
