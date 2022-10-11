plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    alias(libs.plugins.hilt)
}

android {
    namespace = "org.cru.godtools.tool"

    baseConfiguration(project)
    createEventBusIndex("org.cru.godtools.base.tool.BaseToolEventBusIndex")

    defaultConfig.vectorDrawables.useSupportLibrary = true
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    api(project(":library:analytics"))
    api(project(":library:db"))
    api(project(":library:download-manager"))
    api(project(":library:sync"))
    api(project(":ui:base"))
    implementation(project(":library:base"))
    implementation(project(":library:model"))

    implementation(libs.kotlin.coroutines)

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
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.eventbus)
    implementation(libs.gtoSupport.kotlin.coroutines)
    implementation(libs.gtoSupport.lottie)
    implementation(libs.gtoSupport.materialComponents)
    implementation(libs.gtoSupport.util)

    implementation(libs.godtoolsMpp.parser)

    api(libs.taptargetview)
    implementation(libs.dagger)
    implementation(libs.google.auto.value.annotations)
    implementation(libs.hilt)
    implementation(libs.lottie)
    implementation(libs.picasso)
    implementation(libs.picasso.transformations)
    implementation(libs.splitties.bitflags)
    implementation(libs.splitties.fragmentargs)
    implementation(libs.youtubePlayer)

    kapt(libs.dagger.compiler)
    kapt(libs.eventbus.annotationProcessor)
    kapt(libs.google.auto.value)
    kapt(libs.hilt.compiler)

    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.kotlin.coroutines.test)
    kaptTest(libs.androidx.databinding.compiler)
}
