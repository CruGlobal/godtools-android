plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.downloadmanager"

    configureCompose(project)
    buildFeatures.dataBinding = true

    testOptions.unitTests.all {
        // enable spyk mocks for java.io.File
        it.jvmArgs("--add-opens", "java.base/java.io=ALL-UNNAMED")
    }
}

dependencies {
    implementation(project(":library:api"))
    implementation(project(":library:base"))
    implementation(project(":library:db"))
    implementation(project(":library:model"))

    implementation(libs.godtoolsShared.parser)

    api(libs.androidx.hilt.work)
    api(libs.androidx.work.ktx)

    implementation(libs.gtoSupport.androidx.work)
    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.kotlin.coroutines)

    implementation(libs.dagger)
    implementation(libs.guava)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)

    ksp(libs.androidx.hilt.compiler)
    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.hilt.testing)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
    kspTest(libs.hilt.compiler)
}
