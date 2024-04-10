import org.cru.godtools.gradle.bundledcontent.configureBundledContent

plugins {
    id("godtools.library-conventions")
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cru.godtools.initialcontent"

    configureFlavorDimensions(project)
}

androidComponents {
    onVariants {
        it.configureBundledContent(
            project,
            apiUrl = when (it.productFlavors.toMap()[FLAVOR_DIMENSION_ENV]) {
                FLAVOR_ENV_STAGE -> URI_MOBILE_CONTENT_API_STAGE
                else -> URI_MOBILE_CONTENT_API_PRODUCTION
            },
            bundledTools = listOf("kgp", "fourlaws", "satisfied", "teachmetoshare"),
            bundledAttachments = listOf("attr-banner", "attr-banner-about", "attr-about-banner-animation"),
            bundledLanguages = listOf("en"),
            downloadTranslations = false,
        )
    }
}

dependencies {
    implementation(project(":library:base"))
    implementation(project(":library:db"))
    implementation(project(":library:download-manager"))
    implementation(project(":library:model"))

    implementation(libs.kotlin.coroutines)

    implementation(libs.gtoSupport.dagger)
    implementation(libs.gtoSupport.jsonapi)
    implementation(libs.gtoSupport.util)

    implementation(libs.dagger)
    implementation(libs.hilt)

    testImplementation(libs.kotlin.coroutines.test)

    ksp(libs.hilt.compiler)
}
