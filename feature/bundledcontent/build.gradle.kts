plugins {
    id("godtools.dynamic-feature-conventions")
    alias(libs.plugins.ksp)
}

android.namespace = "org.cru.godtools.feature.bundledcontent"

dependencies {
    implementation(project(":library:initial-content"))

    implementation(libs.gtoSupport.dagger)

    implementation(libs.dagger)

    ksp(libs.dagger.compiler)
}
