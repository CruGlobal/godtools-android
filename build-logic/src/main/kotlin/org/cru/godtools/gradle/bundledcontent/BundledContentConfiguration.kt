package org.cru.godtools.gradle.bundledcontent

import com.android.build.api.variant.BuildConfigField
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
    bundledContentBuildConfigFields(bundledTools, bundledLanguages)
    registerDownloadBundledLanguageJsonTask(project, apiUrl)
    val toolsJsonOutput = registerDownloadBundledToolsJsonTask(project, apiUrl)
    registerDownloadBundledAttachmentsTask(project, apiUrl, toolsJsonOutput, bundledTools, bundledAttachments)
    if (downloadTranslations) {
        registerDownloadBundledTranslationsTask(project, apiUrl, toolsJsonOutput, bundledTools, bundledLanguages)
    }
}

private fun LibraryVariant.bundledContentBuildConfigFields(bundledTools: List<String>, bundledLanguages: List<String>) {
    // include the list of bundled tools and languages as BuildConfig constants
    buildConfigFields.put(
        "BUNDLED_TOOLS",
        BuildConfigField(
            "java.util.List<String>",
            "java.util.Arrays.asList(" + bundledTools.joinToString(",") { "\"$it\"" } + ")",
            null
        )
    )
    buildConfigFields.put(
        "BUNDLED_LANGUAGES",
        BuildConfigField(
            "java.util.List<String>",
            "java.util.Arrays.asList(" + bundledLanguages.joinToString(",") { "\"$it\"" } + ")",
            null
        )
    )
}

// configure download bundled language json tasks
private fun LibraryVariant.registerDownloadBundledLanguageJsonTask(project: Project, apiUrl: String) {
    val intermediateLanguagesJson = project.buildDir.resolve("intermediates/mobile_content_api/$name/languages.json")
    val downloadTask = project.tasks.register<Download>("download${name.capitalize()}LanguagesJson") {
        mustRunAfter("clean")

        src("${apiUrl}languages")
        dest(intermediateLanguagesJson)
        retries(2)
        quiet(false)
        tempAndMove(true)
    }
    val pruneTask = project.tasks.register<PruneJsonApiResponseTask>("prune${name.capitalize()}LanguagesJson") {
        dependsOn(downloadTask)
        input.set(intermediateLanguagesJson)
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
    val intermediateToolsJson = project.buildDir.resolve("intermediates/mobile_content_api/$name/tools.json")
    val downloadToolsJsonTask = project.tasks.register<Download>("download${name.capitalize()}BundledToolsJson") {
        mustRunAfter("clean")

        src("${apiUrl}resources?filter[system]=GodTools&include=attachments,latest-translations.language")
        dest(intermediateToolsJson)
        retries(2)
        tempAndMove(true)
    }
    val pruneTask = project.tasks.register<PruneJsonApiResponseTask>("prune${name.capitalize()}BundledToolsJson") {
        dependsOn(downloadToolsJsonTask)
        input.set(intermediateToolsJson)
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
    val intermediatesDir = project.buildDir.resolve("intermediates/mobile_content_api/$name/")

    bundledTools.forEach { tool ->
        val variant = "${name.capitalize()}${tool.capitalize()}"
        val extractTask = project.tasks.register<ExtractAttachmentsTask>("extract${variant}BundledAttachments") {
            this.toolsJson.set(toolsJson)
            this.tool = tool
            attachments = bundledAttachments
            output.set(intermediatesDir.resolve("tool/$tool/attachments.json"))
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
    val intermediatesDir = project.buildDir.resolve("intermediates/mobile_content_api/$name/")
    bundledTools.forEach { tool ->
        bundledLanguages.forEach { lang ->
            val variant = "${name.capitalize()}${tool.capitalize()}${lang.capitalize()}"
            val extractTask = project.tasks.register<ExtractTranslationTask>("extract${variant}BundledTranslation") {
                this.toolsJson.set(toolsJson)
                this.tool = tool
                language = lang
                output.set(intermediatesDir.resolve("tool/$tool/$lang.translation.json"))
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
