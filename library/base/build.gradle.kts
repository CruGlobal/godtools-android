plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android.baseConfiguration(project)

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.kotlin.coroutines)
    implementation(libs.gtoSupport.okta) {
        exclude(group = "org.ccci.gto.android", module = "gto-support-okta-oidc")
    }
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)
    implementation(libs.okta.auth.foundation.bootstrap)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(libs.kotlin.coroutines.test)
}
