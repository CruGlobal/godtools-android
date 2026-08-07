plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.tool.tips"

    configureCompose(project)
    enableDatabinding(project)

    defaultConfig.vectorDrawables.useSupportLibrary = true
}

dependencies {
    api(projects.ui.baseTool)

    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.fragment.ktx)

    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.materialComponents)

    implementation(libs.hilt)
    implementation(libs.materialComponents)
    implementation(libs.splitties.fragmentargs)

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)
}
