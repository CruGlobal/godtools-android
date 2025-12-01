plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.ksp)
}

android.namespace = "org.cru.godtools.user"

dependencies {
    api(projects.library.model)
    implementation(projects.library.account)
    implementation(projects.library.db)
    implementation(projects.library.sync)

    api(libs.godtoolsShared.user.activity)

    implementation(libs.gtoSupport.dagger)

    implementation(libs.dagger)

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
}
