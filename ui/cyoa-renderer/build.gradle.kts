plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.tool.cyoa"

    configureCompose(project)
    configureQaBuildType(project)
    configureGodToolsCustomUri()
    enableDatabinding(project)
}

dependencies {
    api(projects.ui.baseTool)
    implementation(projects.ui.tipsRenderer)

    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.gtoSupport.androidx.databinding)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.androidx.recyclerview)
    implementation(libs.gtoSupport.androidx.viewpager2)
    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.materialComponents)
    implementation(libs.gtoSupport.util)

    implementation(libs.hilt)
    implementation(libs.splitties.fragmentargs)

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(projects.library.account)
    testImplementation(projects.library.model)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.gtoSupport.kotlin.coroutines)
    testImplementation(libs.gtoSupport.testing.dagger)
    testImplementation(libs.hilt.testing)
    kspTest(libs.hilt.compiler)
}
