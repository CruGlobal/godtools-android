dependencies {
    api(project(":library:base"))
    implementation(project(":library:model"))

    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.core)
    api(libs.gtoSupport.db)
    api(libs.gtoSupport.db.coroutines)
    api(libs.gtoSupport.db.livedata)
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
    implementation(libs.hilt)

    kapt(libs.dagger.compiler)
}
