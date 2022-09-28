plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    namespace = "org.cru.godtools.api"

    baseConfiguration(project)

    defaultConfig {
        buildConfigField("String", "CAMPAIGN_FORMS_API", "\"https://campaign-forms.cru.org/\"")
        buildConfigField("String", "CAMPAIGN_FORMS_ID", "\"3fb6022c-5ef9-458c-928a-0380c4a0e57b\"")

        buildConfigField("String", "MOBILE_CONTENT_SYSTEM", "\"GodTools\"")
    }
}

dependencies {
    api(project(":library:model"))

    api(libs.gtoSupport.jsonapi.retrofit2)
    api(libs.gtoSupport.scarlet)
    api(libs.gtoSupport.scarlet.actioncable)
    implementation(libs.gtoSupport.api.okhttp3)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.jsonapi.scarlet)
    implementation(libs.gtoSupport.okta) {
        exclude(group = "org.ccci.gto.android", module = "gto-support-okta-oidc")
    }
    implementation(libs.gtoSupport.retrofit2)
    implementation(libs.gtoSupport.util)

    api(libs.retrofit2)
    api(libs.scarlet)
    implementation(libs.dagger)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)
    implementation(libs.okta.auth.foundation.bootstrap)
    implementation(libs.scarlet.lifecycle.android)
    implementation(libs.scarlet.adapters.stream.coroutines)
    implementation(libs.scarlet.websockets.okhttp)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)
}
