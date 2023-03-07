plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
}

android.namespace = "org.cru.godtools.account"

dependencies {
    implementation(project(":library:api"))

    implementation(libs.gtoSupport.core)

    implementation(libs.dagger)
    implementation(libs.hilt)

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
