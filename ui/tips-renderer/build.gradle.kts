plugins {
    alias(libs.plugins.hilt)
}

android {
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
    implementation(libs.gtoSupport.androidx.viewpager2)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.materialComponents)
    implementation(libs.gtoSupport.recyclerview)
    implementation(libs.gtoSupport.util)

    implementation(libs.godtoolsMpp.parser)
    implementation(libs.hilt)
    implementation(libs.splitties.fragmentargs)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)
}
