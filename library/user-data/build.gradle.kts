dependencies {
    implementation(project(":library:db"))
    implementation(project(":library:model"))
    implementation(project(":library:sync"))

    implementation(libs.dagger)

    kapt(libs.dagger.compiler)
}
