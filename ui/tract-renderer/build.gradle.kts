plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
    id("kotlin-parcelize")
    alias(libs.plugins.hilt)
}

android {
    namespace = "org.cru.godtools.tool.tract"

    configureCompose(project)
    configureQaBuildType(project)
    configureGodToolsCustomUri()
    createEventBusIndex("org.cru.godtools.tract.TractEventBusIndex")

    defaultConfig.vectorDrawables.useSupportLibrary = true
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    api(projects.ui.baseTool)
    api(projects.ui.tutorialRenderer)
    implementation(projects.library.api)
    implementation(projects.library.base)
    implementation(projects.library.db)
    implementation(projects.library.model)
    implementation(projects.library.sync)
    implementation(projects.ui.tipsRenderer)

    implementation(libs.godtoolsShared.user.activity)

    implementation(libs.androidx.cardview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    api(libs.gtoSupport.viewpager)
    implementation(libs.gtoSupport.androidx.databinding)
    implementation(libs.gtoSupport.androidx.fragment)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.androidx.recyclerview)
    implementation(libs.gtoSupport.base)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.eventbus)
    implementation(libs.gtoSupport.kotlin.coroutines)
    implementation(libs.gtoSupport.lottie)
    implementation(libs.gtoSupport.materialComponents)
    implementation(libs.gtoSupport.picasso)
    implementation(libs.gtoSupport.util)

    implementation(libs.circuit.overlay)
    implementation(libs.colormath.android.colorint)
    implementation(libs.dagger)
    implementation(libs.hilt)
    implementation(libs.lottie)
    implementation(libs.materialComponents)
    implementation(libs.picasso.transformations)
    implementation(libs.play.instantapps)
    implementation(libs.rtlViewpager)
    implementation(libs.splitties.fragmentargs)
    implementation(libs.tinder.statemachine)
    implementation(libs.weakdelegate)

    // TODO: transition to KSP for dagger once Data Binding is no longer used
    //       see: https://dagger.dev/dev-guide/ksp#interaction-with-javackapt-processors
    //       see: https://issuetracker.google.com/issues/173030256#comment10
    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(projects.library.account)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.gtoSupport.testing.dagger)
    testImplementation(libs.gtoSupport.testing.picasso)
    testImplementation(libs.hilt.testing)
    testImplementation(libs.kotlin.coroutines.test)
    kaptTest(libs.androidx.databinding.compiler)
    kaptTest(libs.hilt.compiler)
}
