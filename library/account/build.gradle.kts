plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    namespace = "org.cru.godtools.account"
    baseConfiguration(project)
}

dependencies {
    implementation(project(":library:api"))

    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.okta) {
        exclude(group = "org.ccci.gto.android", module = "gto-support-okta-oidc")
    }

    implementation(libs.dagger)
    implementation(libs.hilt)
    implementation(libs.okta.auth.foundation.bootstrap)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
}
