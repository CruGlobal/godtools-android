package org.cru.godtools.gradle.bundledcontent

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.gradle.api.LibraryVariant
import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
//context(Project)
fun LibraryVariant.configureBundledContent(
    project: Project,
    apiUrl: String,
    bundledTools: List<String>,
    bundledLanguages: List<String>,
    bundledAttachments: List<String>,
    downloadTranslations: Boolean
) {
    bundledContentBuildConfigFields(bundledTools, bundledLanguages)

    // create a generated assets directory for the bundled content and add it to the most specific source set available.
    val assetGenDir = project.buildDir.resolve("generated/assets/initial-content/$dirName")
    val assetGenTask = project.tasks.named("generate${name.capitalize()}Assets")
    sourceSets.filterIsInstance<AndroidSourceSet>().last().assets.srcDir(assetGenDir)

    // configure download bundled content tasks
    val languagesJsonTask = registerDownloadBundledLanguageJsonTask(project, apiUrl) {
        output.set(assetGenDir.resolve("languages.json"))
    }
    val toolsJsonTask = registerDownloadBundledToolsJsonTask(project, apiUrl) {
        output.set(assetGenDir.resolve("tools.json"))
    }
    val toolsJsonOutput = toolsJsonTask.flatMap { it.output }
    val attachmentsTask =
        registerDownloadBundledAttachmentsTask(project, apiUrl, toolsJsonOutput, bundledTools, bundledAttachments) {
            into(assetGenDir.resolve("attachments/"))
        }
    if (downloadTranslations) {
        val translationsTask =
            registerDownloadBundledTranslationsTask(project, apiUrl, toolsJsonOutput, bundledTools, bundledLanguages) {
                into(assetGenDir.resolve("translations/"))
            }
        assetGenTask.configure { dependsOn(translationsTask) }
    }

    assetGenTask.configure { dependsOn(languagesJsonTask, toolsJsonTask, attachmentsTask) }
}

private fun LibraryVariant.bundledContentBuildConfigFields(bundledTools: List<String>, bundledLanguages: List<String>) {
    // include the list of bundled tools and languages as BuildConfig constants
    buildConfigField(
        "java.util.List<String>",
        "BUNDLED_TOOLS",
        "java.util.Arrays.asList(" + bundledTools.joinToString(",") { "\"$it\"" } + ")"
    )
    buildConfigField(
        "java.util.List<String>",
        "BUNDLED_LANGUAGES",
        "java.util.Arrays.asList(" + bundledLanguages.joinToString(",") { "\"$it\"" } + ")"
    )
}

// configure download bundled language json tasks
private fun LibraryVariant.registerDownloadBundledLanguageJsonTask(
    project: Project,
    apiUrl: String,
    configuration: PruneJsonApiResponseTask.() -> Unit
): TaskProvider<PruneJsonApiResponseTask> {
    val intermediateLanguagesJson = project.buildDir.resolve("intermediates/mobile_content_api/$dirName/languages.json")
    val downloadTask = project.tasks.register<Download>("download${name.capitalize()}LanguagesJson") {
        mustRunAfter("clean")

        src("${apiUrl}languages")
        dest(intermediateLanguagesJson)
        retries(2)
        quiet(false)
        tempAndMove(true)
    }
    return project.tasks.register<PruneJsonApiResponseTask>("prune${name.capitalize()}LanguagesJson") {
        dependsOn(downloadTask)
        input.set(intermediateLanguagesJson)
        removeAllRelationshipsFor = listOf("language")
        removeAttributesFor["language"] = listOf("direction")
        configuration()
    }
}

// configure download bundled tools json tasks
private fun LibraryVariant.registerDownloadBundledToolsJsonTask(
    project: Project,
    apiUrl: String,
    configuration: PruneJsonApiResponseTask.() -> Unit
): TaskProvider<PruneJsonApiResponseTask> {
    val intermediateToolsJson = project.buildDir.resolve("intermediates/mobile_content_api/$dirName/tools.json")
    val downloadToolsJsonTask = project.tasks.register<Download>("download${name.capitalize()}BundledToolsJson") {
        mustRunAfter("clean")

        src("${apiUrl}resources?filter[system]=GodTools&include=attachments,latest-translations.language")
        dest(intermediateToolsJson)
        retries(2)
        tempAndMove(true)
    }
    return project.tasks.register<PruneJsonApiResponseTask>("prune${name.capitalize()}BundledToolsJson") {
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
        configuration()
    }
}

// configure download bundled attachments tasks
private fun LibraryVariant.registerDownloadBundledAttachmentsTask(
    project: Project,
    apiUrl: String,
    toolsJson: Provider<RegularFile>,
    bundledTools: List<String>,
    bundledAttachments: List<String>,
    configuration: Copy.() -> Unit
): TaskProvider<Copy> {
    val intermediatesDir = project.buildDir.resolve("intermediates/mobile_content_api/$dirName/")
    val attachmentsCopyTask = project.tasks.register("copy${name.capitalize()}BundledAttachments", configuration)

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
            output.set(intermediatesDir.resolve("attachments/$tool/"))
        }
        attachmentsCopyTask.configure { from(downloadTask.flatMap { it.output }) }
    }

    return attachmentsCopyTask
}

private fun LibraryVariant.registerDownloadBundledTranslationsTask(
    project: Project,
    apiUrl: String,
    toolsJson: Provider<RegularFile>,
    bundledTools: List<String>,
    bundledLanguages: List<String>,
    configuration: Copy.() -> Unit
): TaskProvider<Copy> {
    val intermediatesDir = project.buildDir.resolve("intermediates/mobile_content_api/$dirName/")
    val translationsCopyTask = project.tasks.register("copy${name.capitalize()}BundledTranslations", configuration)
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
                    output.set(intermediatesDir.resolve("translation/$tool/$lang/"))
                }

            translationsCopyTask.configure { from(downloadTask.flatMap { it.output }) }
        }
    }

    return translationsCopyTask
}
