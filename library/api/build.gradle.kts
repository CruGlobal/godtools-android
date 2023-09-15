plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
}

android {
    namespace = "org.cru.godtools.api"

    buildFeatures.buildConfig = true

    defaultConfig {
        buildConfigField("String", "CAMPAIGN_FORMS_API", "\"https://campaign-forms.cru.org/\"")
        buildConfigField("String", "CAMPAIGN_FORMS_ID", "\"3fb6022c-5ef9-458c-928a-0380c4a0e57b\"")

        buildConfigField("String", "MOBILE_CONTENT_SYSTEM", "\"GodTools\"")
    }
}

dependencies {
    api(project(":library:model"))

    api(libs.gtoSupport.api.okhttp3)
    api(libs.gtoSupport.jsonapi.retrofit2)
    api(libs.gtoSupport.scarlet)
    api(libs.gtoSupport.scarlet.actioncable)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.jsonapi.scarlet)
    implementation(libs.gtoSupport.retrofit2)
    implementation(libs.gtoSupport.util)

    api(libs.retrofit2)
    api(libs.scarlet)
    implementation(libs.dagger)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)
    implementation(libs.scarlet.lifecycle.android)
    implementation(libs.scarlet.adapters.stream.coroutines)
    implementation(libs.scarlet.websockets.okhttp)

    // TODO: transition to KSP for dagger once referencing BuildConfig is supported
    //       see: https://github.com/google/dagger/issues/4051
    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(libs.json)
}
