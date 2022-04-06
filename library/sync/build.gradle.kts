configureAndroidLibrary()

dependencies {
    implementation(project(":library:api"))
    implementation(project(":library:db"))
    implementation(project(":library:model"))

    implementation(libs.androidx.collection.ktx)
    implementation(libs.androidx.core.ktx)
    api(libs.androidx.hilt.work)
    api(libs.androidx.work.ktx)

    api(libs.gtoSupport.sync)
    implementation(libs.gtoSupport.base)
    implementation(libs.gtoSupport.okta)

    implementation(libs.dagger)
    implementation(libs.eventbus)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)
    implementation(libs.okta)

    testImplementation(libs.kotlin.coroutines.test)

    kapt(libs.androidx.hilt.compiler)
    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)
}
