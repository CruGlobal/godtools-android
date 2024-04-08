plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
    alias(libs.plugins.hilt)
}

android {
    namespace = "org.cru.godtools.tool.cyoa"

    configureQaBuildType(project)
    configureGodToolsCustomUri()

    buildFeatures.dataBinding = true
}

onesky {
    sourceStringFiles = listOf(
        "strings_cyoa_renderer.xml",
    )
}

dependencies {
    api(project(":ui:base-tool"))
    implementation(project(":ui:tips-renderer"))

    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.gtoSupport.androidx.databinding)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.androidx.recyclerview)
    implementation(libs.gtoSupport.androidx.viewpager2)
    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.materialComponents)
    implementation(libs.gtoSupport.util)

    implementation(libs.hilt)
    implementation(libs.splitties.fragmentargs)

    // TODO: transition to KSP for dagger once Data Binding is no longer used
    //       see: https://dagger.dev/dev-guide/ksp#interaction-with-javackapt-processors
    //       see: https://issuetracker.google.com/issues/173030256#comment10
    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(project(":library:account"))
    testImplementation(project(":library:model"))
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.gtoSupport.kotlin.coroutines)
    testImplementation(libs.gtoSupport.testing.dagger)
    testImplementation(libs.hilt.testing)
    kaptTest(libs.hilt.compiler)
}
