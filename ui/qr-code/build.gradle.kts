plugins {
    id("godtools.library-conventions")
}

android {
    namespace = "org.cru.godtools.qrcode"

    configureCompose(project)
}

tasks.withType<Test> {
    failOnNoDiscoveredTests.set(false)
}

dependencies {
    implementation(project(":ui:base"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)

    implementation(libs.timber)
    implementation(libs.zxing)
}
