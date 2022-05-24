import dagger.hilt.android.plugin.util.capitalize
import org.cru.godtools.gradle.bundledcontent.DownloadApiResourcesTask
import org.cru.godtools.gradle.bundledcontent.ExtractTranslationTask
import org.cru.godtools.gradle.bundledcontent.PruneJsonApiResponseTask
import org.cru.godtools.gradle.bundledcontent.configureBundledContent

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    baseConfiguration(project)
    configureFlavorDimensions()
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

val BUNDLED_TOOLS = listOf("kgp", "fourlaws", "satisfied", "teachmetoshare")
val BUNDLED_ATTACHMENTS = listOf("attr-banner", "attr-banner-about")
val BUNDLED_LANGUAGES = listOf("en")
val DOWNLOAD_TRANSLATIONS = false

android.libraryVariants.configureEach {
    val mobileContentApi =
        if (flavorName.contains("stage")) URI_MOBILE_CONTENT_API_STAGE else URI_MOBILE_CONTENT_API_PRODUCTION

    configureBundledContent(
        project,
        apiUrl = mobileContentApi,
        bundledTools = BUNDLED_TOOLS,
        bundledAttachments = BUNDLED_ATTACHMENTS,
        bundledLanguages = BUNDLED_LANGUAGES,
        downloadTranslations = DOWNLOAD_TRANSLATIONS
    )

    val assetGenTask = tasks.named("generate${name.capitalize()}Assets")
    val assetGenDir = project.buildDir.resolve("generated/assets/initial-content/$dirName")
    val intermediatesDir = buildDir.resolve("intermediates/mobile_content_api/$dirName/")

    // configure tools json download tasks
    val pruneToolsJsonTask = tasks.named<PruneJsonApiResponseTask>("prune${name.capitalize()}BundledToolsJson")

    val translationsCopyTask = tasks.register<Copy>("copy${name.capitalize()}BundledTranslations") {
        enabled = DOWNLOAD_TRANSLATIONS
        into(assetGenDir.resolve("translations/"))
    }
    assetGenTask.configure { dependsOn(translationsCopyTask) }
    BUNDLED_TOOLS.forEach { tool ->
        val variant = "${name.capitalize()}${tool.capitalize()}"

        if (DOWNLOAD_TRANSLATIONS) {
            // configure bundled translations download tasks for each bundled language
            BUNDLED_LANGUAGES.forEach { lang ->
                val langVariant = "$variant${lang.capitalize()}"

                val extractTranslationTask =
                    tasks.register<ExtractTranslationTask>("extract${langVariant}BundledTranslation") {
                        toolsJson.set(pruneToolsJsonTask.flatMap { it.output })
                        this.tool = tool
                        language = lang
                        output.set(intermediatesDir.resolve("tool/$tool/$lang.translation.json"))
                    }

                val downloadTranslationTask =
                    tasks.register<DownloadApiResourcesTask>("download${langVariant}BundledTranslation") {
                        resources.set(extractTranslationTask.flatMap { it.output })
                        api = mobileContentApi
                        output.set(intermediatesDir.resolve("translation/$tool/$lang/"))
                    }

                translationsCopyTask.configure { from(downloadTranslationTask.flatMap { it.output }) }
            }
        }
    }
}
