plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.ksp)
}

android.namespace = "org.cru.godtools.user"

dependencies {
    api(project(":library:model"))
    implementation(project(":library:account"))
    implementation(project(":library:db"))
    implementation(project(":library:sync"))

    api(libs.godtoolsShared.user.activity)

    implementation(libs.gtoSupport.dagger)

    implementation(libs.dagger)

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
}
