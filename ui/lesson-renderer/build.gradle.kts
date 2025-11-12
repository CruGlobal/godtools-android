plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.tool.lesson"

    configureCompose(project)
    configureQaBuildType(project)
    configureGodToolsCustomUri()

    buildFeatures.viewBinding = true
}

dependencies {
    api(project(":ui:base-tool"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime.livedata)

    implementation(libs.gtoSupport.androidx.lifecycle)

    implementation(libs.circuit.overlay)
    implementation(libs.hilt)
    implementation(libs.lottie.compose)

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(project(":library:account"))
    testImplementation(project(":library:model"))
    testImplementation(testFixtures(libs.gtoSupport.circuit))
    testImplementation(libs.hilt.testing)
    testImplementation(libs.okio.fakefilesystem)
    kspTest(libs.hilt.compiler)
}
