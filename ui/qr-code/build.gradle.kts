plugins {
    id("godtools.library-conventions")
}

android {
    namespace = "org.cru.godtools.qrcode"

    configureCompose(project)
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.timber)
    implementation(libs.zxing)
}
