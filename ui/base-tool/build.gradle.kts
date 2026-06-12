plugins {
    id("godtools.library-conventions")
    id("kotlin-parcelize")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.tool"

    configureCompose(project)
    createEventBusIndex("org.cru.godtools.base.tool.BaseToolEventBusIndex")
    enableDatabinding(project)

    defaultConfig.vectorDrawables.useSupportLibrary = true
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    api(projects.library.analytics)
    api(projects.library.db)
    api(projects.library.downloadManager)
    api(projects.library.sync)
    api(projects.library.userData)
    api(projects.ui.base)
    implementation(projects.library.base)
    implementation(projects.library.model)

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
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.eventbus)
    implementation(libs.gtoSupport.kotlin.coroutines)
    implementation(libs.gtoSupport.lottie)
    implementation(libs.gtoSupport.materialComponents)
    implementation(libs.gtoSupport.okio)
    implementation(libs.gtoSupport.util)

    api(libs.okio)
    api(libs.taptargetview)
    implementation(libs.colormath.android.colorint)
    implementation(libs.dagger)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)
    implementation(libs.lottie)
    implementation(libs.picasso)
    implementation(libs.picasso.transformations)
    implementation(libs.splitties.bitflags)
    implementation(libs.splitties.fragmentargs)

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(kotlin("reflect"))
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
}
