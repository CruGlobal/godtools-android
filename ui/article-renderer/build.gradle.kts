plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
    alias(libs.plugins.hilt)
}

android {
    namespace = "org.cru.godtools.tool.article"

    configureQaBuildType(project)
    configureGodToolsCustomUri()

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    api(project(":ui:article-aem-renderer"))
    implementation(project(":library:base"))
    implementation(project(":ui:base-tool"))

    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.gtoSupport.androidx.fragment)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.androidx.recyclerview)
    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.picasso)
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
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
    testImplementation(libs.hilt.testing)
    kaptTest(libs.hilt.compiler)
}
