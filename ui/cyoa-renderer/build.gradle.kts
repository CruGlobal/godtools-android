plugins {
    id("dagger.hilt.android.plugin")
}

android {
    buildFeatures.dataBinding = true
}

dependencies {
    api(project(":ui:base-tool"))

    implementation(libs.gtoSupport.androidx.databinding)
    implementation(libs.gtoSupport.materialComponents)

    implementation(libs.godtoolsMpp.parser)
    implementation(libs.hilt)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)
}
