import org.cru.godtools.gradle.bundledcontent.configureBundledContent

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    baseConfiguration(project)
    configureFlavorDimensions()

    libraryVariants.configureEach {
        val mobileContentApi =
            if (flavorName.contains("stage")) URI_MOBILE_CONTENT_API_STAGE else URI_MOBILE_CONTENT_API_PRODUCTION

        configureBundledContent(
            project,
            apiUrl = mobileContentApi,
            bundledTools = listOf("kgp", "fourlaws", "satisfied", "teachmetoshare"),
            bundledAttachments = listOf("attr-banner", "attr-banner-about"),
            bundledLanguages = listOf("en"),
            downloadTranslations = false
        )
    }
}

dependencies {
    implementation(project(":library:base"))
    implementation(project(":library:db"))
    implementation(project(":library:download-manager"))
    implementation(project(":library:model"))

    implementation(libs.kotlin.coroutines)

    implementation(libs.gtoSupport.compat)
    implementation(libs.gtoSupport.core)
    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.jsonapi)
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
    implementation(libs.hilt)

    testImplementation(libs.kotlin.coroutines.test)

    kapt(libs.hilt.compiler)
}
