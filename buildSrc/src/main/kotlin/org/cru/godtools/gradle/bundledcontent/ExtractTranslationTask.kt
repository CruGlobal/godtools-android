package org.cru.godtools.gradle.bundledcontent

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.json.JSONObject

@CacheableTask
@OptIn(ExperimentalStdlibApi::class)
abstract class ExtractTranslationTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val toolsJson: RegularFileProperty

    @get:Input
    lateinit var tool: String
    @get:Input
    lateinit var language: String

    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    fun extractTranslations() {
        val json = toolsJson.asFile.get().loadJson()
        val included = json.optJSONArrayOrJSONObject("included")

        val toolId = json.optJSONArrayOrJSONObject("data")
            .firstOrNull { it.attributes?.optString("abbreviation") == tool }
            ?.jsonApiId
        val languageId = included.filter { it.jsonApiType == "language" }
            .firstOrNull { it.attributes?.optString("code") == language }
            ?.jsonApiId

        // find translation
        val translation = included.filter { it.jsonApiType == "translation" }
            .firstOrNull {
                it.relationships?.optJSONObject("resource")?.optJSONObject("data")?.jsonApiId == toolId &&
                it.relationships?.optJSONObject("language")?.optJSONObject("data")?.jsonApiId == languageId
            }

        // write the translation resource to output
        JSONObject(buildMap<String, String> {
            translation?.jsonApiId?.let { put("translations/$it", "$it.zip") }
        }).writeJson(output.asFile.get())
    }
}
