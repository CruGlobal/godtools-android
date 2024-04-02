plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.base"

    configureCompose(project)
}

onesky {
    sourceStringFiles = listOf(
        "strings_language_names.xml",
    )

    // an empty list downloads all available languages
    downloadLanguages = emptyList()
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.gtoSupport.androidx.core)
    implementation(libs.gtoSupport.androidx.lifecycle)
    implementation(libs.gtoSupport.kotlin.coroutines)
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
    implementation(libs.hilt)
    implementation(libs.kotlin.coroutines)

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.turbine)
}
