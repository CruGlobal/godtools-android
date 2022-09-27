plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    alias(libs.plugins.hilt)
}

android {
    namespace = "org.cru.godtools.tool.article"

    baseConfiguration(project)

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
    implementation(libs.godtoolsMpp.parser)
    implementation(libs.hilt)
    implementation(libs.splitties.fragmentargs)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(project(":library:model"))
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.hilt.testing)

    kaptTest(libs.hilt.compiler)
}
