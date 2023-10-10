plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
    alias(libs.plugins.hilt)
}

android {
    namespace = "org.cru.godtools.tool.lesson"

    configureQaBuildType(project)
    configureGodToolsCustomUri()

    buildFeatures.dataBinding = true
}

onesky {
    sourceStringFiles = listOf(
        "strings_lesson_feedback.xml",
    )
}

dependencies {
    api(project(":ui:base-tool"))

    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.viewpager2)

    implementation(libs.gtoSupport.androidx.databinding)
    implementation(libs.gtoSupport.androidx.fragment)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.androidx.recyclerview)
    implementation(libs.gtoSupport.androidx.viewpager2)
    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.eventbus)
    implementation(libs.gtoSupport.materialComponents)
    implementation(libs.gtoSupport.util)

    implementation(libs.hilt)
    implementation(libs.materialComponents)
    implementation(libs.splitties.fragmentargs)

    // TODO: transition to KSP for dagger once Data Binding is no longer used
    //       see: https://dagger.dev/dev-guide/ksp#interaction-with-javackapt-processors
    //       see: https://issuetracker.google.com/issues/173030256#comment10
    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(project(":library:account"))
    testImplementation(project(":library:model"))
    testImplementation(libs.hilt.testing)
    kaptTest(libs.hilt.compiler)
}
