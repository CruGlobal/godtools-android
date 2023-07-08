plugins {
    id("godtools.dynamic-feature-conventions")
    kotlin("kapt")
}

android.namespace = "org.cru.godtools.feature.bundledcontent"

dependencies {
    implementation(project(":library:initial-content"))

    implementation(libs.gtoSupport.dagger)

    implementation(libs.dagger)

    kapt(libs.dagger.compiler)
}
