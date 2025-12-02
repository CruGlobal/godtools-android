plugins {
    id("godtools.dynamic-feature-conventions")
    alias(libs.plugins.ksp)
}

android.namespace = "org.cru.godtools.feature.bundledcontent"

dependencies {
    implementation(projects.library.initialContent)

    implementation(libs.gtoSupport.dagger)

    implementation(libs.dagger)

    ksp(libs.dagger.compiler)
}
