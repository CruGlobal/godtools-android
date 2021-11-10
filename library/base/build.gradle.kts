android {
    defaultConfig.buildConfigField("int", "VERSION_CODE", "${rootProject.ext["versionCode"]}")
}

dependencies {
    implementation(libs.kotlin.coroutines)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.kotlin.coroutines)
    implementation(libs.gtoSupport.okta)
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
    implementation(libs.hilt)

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)
}
