plugins {
    id("godtools.library-conventions")
    kotlin("kapt")
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

    kapt(libs.dagger.compiler)
    kapt(libs.hilt.compiler)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
}
