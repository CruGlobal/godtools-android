plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.tool.article"

    configureCompose(project)
    configureQaBuildType(project)
    configureGodToolsCustomUri()

    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    api(projects.ui.articleAemRenderer)
    implementation(projects.library.base)
    implementation(projects.ui.baseTool)

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.gtoSupport.androidx.fragment)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.kotlin.coroutines)
    implementation(libs.gtoSupport.sync)

    implementation(libs.colormath.jetpack.compose)
    implementation(libs.dagger)
    implementation(libs.godtoolsShared.renderer)
    implementation(libs.hilt)
    implementation(libs.splitties.fragmentargs)

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(projects.library.account)
    testImplementation(projects.library.model)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.hilt.testing)
    kspTest(libs.hilt.compiler)
}
