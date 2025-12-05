plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.paparazzi)
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

    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.junit)
    testImplementation(libs.testparameterinjector)
    testImplementation(libs.paparazzi)
    testImplementation(testFixtures(project(":ui:base")))
}
