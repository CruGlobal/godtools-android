android {
    defaultConfig.buildConfigField("int", "VERSION_CODE", "${rootProject.ext["versionCode"]}")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.kotlin.coroutines)
    implementation(libs.gtoSupport.okta)
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(libs.kotlin.coroutines.test)
}
