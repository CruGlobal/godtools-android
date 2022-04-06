plugins {
    alias(libs.plugins.hilt)
}

configureAndroidLibrary()

android {
    buildFeatures.dataBinding = true
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
    implementation(libs.gtoSupport.materialComponents)
    implementation(libs.gtoSupport.util)

    implementation(libs.godtoolsMpp.parser)
    implementation(libs.hilt)
    implementation(libs.splitties.fragmentargs)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.gtoSupport.testing.dagger)
    testImplementation(libs.hilt.testing)
    kaptTest(libs.hilt.compiler)
}
