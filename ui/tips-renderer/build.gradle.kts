plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
    alias(libs.plugins.hilt)
}

android {
    namespace = "org.cru.godtools.tool.tips"

    defaultConfig.vectorDrawables.useSupportLibrary = true
    buildFeatures.dataBinding = true
}

dependencies {
    api(project(":ui:base-tool"))
    implementation(project(":library:model"))

    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.gtoSupport.androidx.databinding)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.androidx.recyclerview)
    implementation(libs.gtoSupport.androidx.viewpager2)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.kotlin.coroutines)
    implementation(libs.gtoSupport.materialComponents)
    implementation(libs.gtoSupport.util)

    implementation(libs.hilt)
    implementation(libs.splitties.fragmentargs)

    // TODO: transition to KSP for dagger once Data Binding is no longer used
    //       see: https://dagger.dev/dev-guide/ksp#interaction-with-javackapt-processors
    //       see: https://issuetracker.google.com/issues/173030256#comment10
    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
}
