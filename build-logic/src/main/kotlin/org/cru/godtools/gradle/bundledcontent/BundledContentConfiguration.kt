package org.cru.godtools.gradle.bundledcontent

import com.android.build.api.variant.LibraryVariant
import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.register

private const val FILE_LANGUAGES = "languages.json"
private const val FILE_TOOLS = "tools.json"
private const val DIR_ATTACHMENTS = "attachments"
private const val DIR_TRANSLATIONS = "translations"

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
// context(Project)
fun LibraryVariant.configureBundledContent(
    project: Project,
    apiUrl: String,
    bundledTools: List<String>,
    bundledLanguages: List<String>,
    bundledAttachments: List<String>,
    downloadTranslations: Boolean,
) {
    registerDownloadBundledLanguageJsonTask(project, apiUrl)
    val toolsJsonOutput = registerDownloadBundledToolsJsonTask(project, apiUrl)
    registerDownloadBundledAttachmentsTask(project, apiUrl, toolsJsonOutput, bundledTools, bundledAttachments)
    if (downloadTranslations) {
        registerDownloadBundledTranslationsTask(project, apiUrl, toolsJsonOutput, bundledTools, bundledLanguages)
    }
}

// configure download bundled language json tasks
private fun LibraryVariant.registerDownloadBundledLanguageJsonTask(project: Project, apiUrl: String) {
    val intermediateJson = project.layout.buildDirectory.file("intermediates/mobile_content_api/$name/languages.json")
    val downloadTask = project.tasks.register<Download>("download${name.capitalize()}LanguagesJson") {
        mustRunAfter("clean")

        src("${apiUrl}languages")
        dest(intermediateJson)
        retries(2)
        quiet(false)
        tempAndMove(true)
    }
    val pruneTask = project.tasks.register<PruneJsonApiResponseTask>("prune${name.capitalize()}LanguagesJson") {
        dependsOn(downloadTask)
        input.set(intermediateJson)
        outputFilename = FILE_LANGUAGES
        removeAllRelationshipsFor = listOf("language")
        removeAttributesFor["language"] = listOf("direction")
    }
    sources.assets?.addGeneratedSourceDirectory(pruneTask) { it.output }
}

// configure download bundled tools json tasks
private fun LibraryVariant.registerDownloadBundledToolsJsonTask(
    project: Project,
    apiUrl: String,
): Provider<RegularFile> {
    val intermediateJson = project.layout.buildDirectory.file("intermediates/mobile_content_api/$name/tools.json")
    val downloadToolsJsonTask = project.tasks.register<Download>("download${name.capitalize()}BundledToolsJson") {
        mustRunAfter("clean")

        src("${apiUrl}resources?filter[system]=GodTools&include=attachments,latest-translations.language")
        dest(intermediateJson)
        retries(2)
        tempAndMove(true)
    }
    val pruneTask = project.tasks.register<PruneJsonApiResponseTask>("prune${name.capitalize()}BundledToolsJson") {
        dependsOn(downloadToolsJsonTask)
        input.set(intermediateJson)
        outputFilename = FILE_TOOLS
        removeAllRelationshipsFor = listOf("language")
        removeAttributesFor["resource"] = listOf(
            // attributes
            "manifest", "onesky-project-id", "total-views",
            // relationships
            "system", "translations", "latest-drafts-translations", "pages", "custom-manifests", "tips"
        )
        removeAttributesFor["attachment"] = listOf("file", "is-zipped")
        removeAttributesFor["language"] = listOf("direction")
    }
    sources.assets?.addGeneratedSourceDirectory(pruneTask) { it.output }
    return pruneTask.flatMap { it.output.file(it.outputFilename) }
}

// configure download bundled attachments tasks
private fun LibraryVariant.registerDownloadBundledAttachmentsTask(
    project: Project,
    apiUrl: String,
    toolsJson: Provider<RegularFile>,
    bundledTools: List<String>,
    bundledAttachments: List<String>,
) {
    val intermediatesDir = project.layout.buildDirectory.dir("intermediates/mobile_content_api/$name/")

    bundledTools.forEach { tool ->
        val variant = "${name.capitalize()}${tool.capitalize()}"
        val extractTask = project.tasks.register<ExtractAttachmentsTask>("extract${variant}BundledAttachments") {
            this.toolsJson.set(toolsJson)
            this.tool = tool
            attachments = bundledAttachments
            output.set(intermediatesDir.map { it.file("tool/$tool/attachments.json") })
        }
        val downloadTask = project.tasks.register<DownloadApiResourcesTask>("download${variant}BundledAttachments") {
            resources.set(extractTask.flatMap { it.output })
            api = apiUrl
            outputSubDir = DIR_ATTACHMENTS
        }
        sources.assets?.addGeneratedSourceDirectory(downloadTask) { it.output }
    }
}

private fun LibraryVariant.registerDownloadBundledTranslationsTask(
    project: Project,
    apiUrl: String,
    toolsJson: Provider<RegularFile>,
    bundledTools: List<String>,
    bundledLanguages: List<String>,
) {
    val intermediatesDir = project.layout.buildDirectory.dir("intermediates/mobile_content_api/$name/")
    bundledTools.forEach { tool ->
        bundledLanguages.forEach { lang ->
            val variant = "${name.capitalize()}${tool.capitalize()}${lang.capitalize()}"
            val extractTask = project.tasks.register<ExtractTranslationTask>("extract${variant}BundledTranslation") {
                this.toolsJson.set(toolsJson)
                this.tool = tool
                language = lang
                output.set(intermediatesDir.map { it.file("tool/$tool/$lang.translation.json") })
            }

            val downloadTask =
                project.tasks.register<DownloadApiResourcesTask>("download${variant}BundledTranslation") {
                    resources.set(extractTask.flatMap { it.output })
                    api = apiUrl
                    outputSubDir = DIR_TRANSLATIONS
                }
            sources.assets?.addGeneratedSourceDirectory(downloadTask) { it.output }
        }
    }
}
