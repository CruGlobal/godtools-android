plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.analytics"

    buildFeatures.buildConfig = true

    configureCompose(project)
    createEventBusIndex("org.cru.godtools.analytics.AnalyticsEventBusIndex")
}

dependencies {
    implementation(projects.library.account)
    implementation(projects.library.base)
    implementation(projects.library.userData)
    implementation(projects.ui.base)

    api(libs.godtoolsShared.analytics)

    implementation(libs.firebase.messaging)
    implementation(libs.firebase.perf)

    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.dagger)

    implementation(libs.dagger)
    implementation(libs.eventbus)
    implementation(libs.facebook.core)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)
    implementation(libs.okhttp3)
    implementation(libs.play.installreferrer)
    implementation(libs.play.tagmanager)
    implementation(libs.weakdelegate)

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(libs.kotlin.coroutines.test)
}
