plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.analytics"

    buildFeatures.buildConfig = true

    configureCompose(project)
    createEventBusIndex("org.cru.godtools.analytics.AnalyticsEventBusIndex")

    defaultConfig {
        buildConfigField("String", "APPSFLYER_DEV_KEY", "\"QdbVaVHi9bHRchUTWtoaij\"")
    }
}

dependencies {
    api(project(":library:base"))
    implementation(project(":library:account"))
    implementation(project(":library:user-data"))
    implementation(project(":ui:base"))

    api(libs.godtoolsShared.analytics)

    implementation(libs.firebase.messaging)
    implementation(libs.firebase.perf)

    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.dagger)

    implementation(libs.appsflyer)
    implementation(libs.dagger)
    implementation(libs.eventbus)
    implementation(libs.facebook)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)
    implementation(libs.okhttp3)
    implementation(libs.play.installreferrer)
    implementation(libs.play.tagmanager)
    implementation(libs.weakdelegate)

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.coroutines.test)
}
