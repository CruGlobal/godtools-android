plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
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

    // region Okta
    implementation(libs.gtoSupport.okta) {
        exclude(group = "org.ccci.gto.android", module = "gto-support-okta-oidc")
    }
    implementation(libs.okta.auth.foundation)
    implementation(libs.okta.auth.foundation.bootstrap)
    implementation(libs.okta.web.authentication.ui)

    // region Token Migration
    implementation(libs.gtoSupport.okta.oidc)
    implementation(libs.okta.legacy.tokens)
    // endregion Token Migration
    // endregion Okta

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
}
