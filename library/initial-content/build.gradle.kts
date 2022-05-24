import dagger.hilt.android.plugin.util.capitalize
import de.undercouch.gradle.tasks.download.Download
import org.cru.godtools.gradle.bundledcontent.DownloadApiResourcesTask
import org.cru.godtools.gradle.bundledcontent.ExtractAttachmentsTask
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
        bundledLanguages = BUNDLED_LANGUAGES
    )

    val assetGenTask = tasks.named("generate${name.capitalize()}Assets")
    val assetGenDir = project.buildDir.resolve("generated/assets/initial-content/$dirName")
    val intermediatesDir = buildDir.resolve("intermediates/mobile_content_api/$dirName/")

    // configure tools json download tasks
    val intermediateToolsJson = intermediatesDir.resolve("tools.json")
    val downloadToolsJsonTask = tasks.create<Download>("download${name.capitalize()}BundledToolsJson") {
        mustRunAfter("clean")

        src("${mobileContentApi}resources?filter[system]=GodTools&include=attachments,latest-translations.language")
        dest(intermediateToolsJson)
        retries(2)
        tempAndMove(true)
    }
    val pruneToolsJsonTask = tasks.create<PruneJsonApiResponseTask>("prune${name.capitalize()}BundledToolsJson") {
        dependsOn(downloadToolsJsonTask)
        input.set(intermediateToolsJson)
        removeAllRelationshipsFor = listOf("language")
        removeAttributesFor["resource"] = listOf(
            // attributes
            "manifest", "onesky-project-id", "total-views",
            // relationships
            "system", "translations", "latest-drafts-translations", "pages", "custom-manifests", "tips"
        )
        removeAttributesFor["attachment"] = listOf("file", "is-zipped")
        removeAttributesFor["language"] = listOf("direction")
        output.set(assetGenDir.resolve("tools.json"))
    }
    assetGenTask.configure { dependsOn(pruneToolsJsonTask) }

    val attachmentsCopyTask = tasks.register<Copy>("copy${name.capitalize()}BundledAttachments") {
        into(assetGenDir.resolve("attachments/"))
    }
    val translationsCopyTask = tasks.register<Copy>("copy${name.capitalize()}BundledTranslations") {
        enabled = DOWNLOAD_TRANSLATIONS
        into(assetGenDir.resolve("translations/"))
    }
    assetGenTask.configure {
        dependsOn(attachmentsCopyTask)
        dependsOn(translationsCopyTask)
    }
    BUNDLED_TOOLS.forEach { tool ->
        val variant = "${name.capitalize()}${tool.capitalize()}"

        // configure bundled attachments download tasks
        val extractAttachmentsTask = tasks.register<ExtractAttachmentsTask>("extract${variant}BundledAttachments") {
            toolsJson.set(pruneToolsJsonTask.output)
            this.tool = tool
            attachments = BUNDLED_ATTACHMENTS
            output.set(intermediatesDir.resolve("tool/$tool/attachments.json"))
        }

        val downloadAttachmentsTask = tasks.register<DownloadApiResourcesTask>("download${variant}BundledAttachments") {
            resources.set(extractAttachmentsTask.flatMap { it.output })
            api = mobileContentApi
            output.set(intermediatesDir.resolve("attachments/$tool/"))
        }

        attachmentsCopyTask.configure { from(downloadAttachmentsTask.flatMap { it.output }) }

        if (DOWNLOAD_TRANSLATIONS) {
            // configure bundled translations download tasks for each bundled language
            BUNDLED_LANGUAGES.forEach { lang ->
                val langVariant = "$variant${lang.capitalize()}"

                val extractTranslationTask =
                    tasks.register<ExtractTranslationTask>("extract${langVariant}BundledTranslation") {
                        toolsJson.set(pruneToolsJsonTask.output)
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
