configureAndroidFeature()

dependencies {
    implementation(project(":library:initial-content"))

    implementation(libs.gtoSupport.dagger)

    implementation(libs.dagger)

    kapt(libs.dagger.compiler)
}
