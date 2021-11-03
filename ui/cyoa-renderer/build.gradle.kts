plugins {
    id("dagger.hilt.android.plugin")
}

android {
    buildFeatures.dataBinding = true
}

dependencies {
    api(project(":ui:base-tool"))

    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.gtoSupport.androidx.databinding)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.materialComponents)

    implementation(libs.godtoolsMpp.parser)
    implementation(libs.hilt)
    implementation(libs.splitties.fragmentargs)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)
}
