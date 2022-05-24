package org.cru.godtools.gradle.bundledcontent

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.gradle.api.LibraryVariant
import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

// TODO: provide Project using the new multiple context receivers functionality.
//       this is prototyped in 1.6.20 and will probably reach beta in Kotlin 1.8 or 1.9
//context(Project)
fun LibraryVariant.configureBundledContent(
    project: Project,
    apiUrl: String,
    bundledTools: List<String>,
    bundledLanguages: List<String>
) {
    bundledContentBuildConfigFields(bundledTools, bundledLanguages)

    // create a generated assets directory for the bundled content and add it to the most specific source set available.
    val assetGenDir = project.buildDir.resolve("generated/assets/initial-content/$dirName")
    sourceSets.filterIsInstance<AndroidSourceSet>().last().assets.srcDir(assetGenDir)

    // configure download bundled language json tasks
    val downloadBundledLanguagesJsonTask = registerDownloadBundledLanguageJsonTask(project, apiUrl) {
        output.set(assetGenDir.resolve("languages.json"))
    }

    val assetGenTask = project.tasks.named("generate${name.capitalize()}Assets")
    assetGenTask.configure { dependsOn(downloadBundledLanguagesJsonTask) }
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

// configure language json download tasks
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
