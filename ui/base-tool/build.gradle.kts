plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
    id("kotlin-parcelize")
    alias(libs.plugins.hilt)
}

android {
    namespace = "org.cru.godtools.tool"

    configureCompose(project)
    createEventBusIndex("org.cru.godtools.base.tool.BaseToolEventBusIndex")

    defaultConfig.vectorDrawables.useSupportLibrary = true
    buildFeatures {
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }
}

onesky {
    sourceStringFiles = listOf(
        "strings_tool_renderer.xml",
    )
}

dependencies {
    api(project(":library:analytics"))
    api(project(":library:db"))
    api(project(":library:download-manager"))
    api(project(":library:sync"))
    api(project(":library:user-data"))
    api(project(":ui:base"))
    implementation(project(":library:base"))
    implementation(project(":library:model"))

    api(libs.godtoolsShared.parser)
    api(libs.godtoolsShared.renderer)
    implementation(libs.godtoolsShared.user.activity)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.recyclerview)

    api(libs.gtoSupport.picasso)
    implementation(libs.gtoSupport.androidx.collection)
    implementation(libs.gtoSupport.androidx.constraintlayout)
    implementation(libs.gtoSupport.androidx.databinding)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.androidx.recyclerview)
    implementation(libs.gtoSupport.animation)
    implementation(libs.gtoSupport.base)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.eventbus)
    implementation(libs.gtoSupport.kotlin.coroutines)
    implementation(libs.gtoSupport.lottie)
    implementation(libs.gtoSupport.materialComponents)
    implementation(libs.gtoSupport.util)

    api(libs.taptargetview)
    implementation(libs.colormath.android.colorint)
    implementation(libs.dagger)
    implementation(libs.google.auto.value.annotations)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)
    implementation(libs.lottie)
    implementation(libs.picasso)
    implementation(libs.picasso.transformations)
    implementation(libs.splitties.bitflags)
    implementation(libs.splitties.fragmentargs)
    implementation(libs.youtubePlayer)

    // TODO: transition to KSP for dagger once Data Binding is no longer used
    //       see: https://dagger.dev/dev-guide/ksp#interaction-with-javackapt-processors
    //       see: https://issuetracker.google.com/issues/173030256#comment10
    kapt(libs.dagger.compiler)
    kapt(libs.google.auto.value)
    kapt(libs.hilt.compiler)

    testImplementation(kotlin("reflect"))
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
    kaptTest(libs.androidx.databinding.compiler)
}
