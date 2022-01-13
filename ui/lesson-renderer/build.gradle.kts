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
    implementation(libs.androidx.viewpager2)

    implementation(libs.gtoSupport.androidx.databinding)
    implementation(libs.gtoSupport.androidx.fragment)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.androidx.viewpager2)
    implementation(libs.gtoSupport.eventbus)
    implementation(libs.gtoSupport.materialComponents)
    implementation(libs.gtoSupport.recyclerview)
    implementation(libs.gtoSupport.util)

    implementation(libs.godtoolsMpp.parser)
    implementation(libs.hilt)
    implementation(libs.materialComponents)
    implementation(libs.splitties.fragmentargs)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(project(":library:model"))
    testImplementation(libs.hilt.testing)

    kaptTest(libs.hilt.compiler)
}
