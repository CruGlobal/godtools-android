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
abstract class ExtractAttachmentsTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val toolsJson: RegularFileProperty

    @get:Input
    lateinit var tool: String
    @get:Input
    var attachments = emptyList<String>()

    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    fun extractAttachments() {
        val json = toolsJson.asFile.get().loadJson()

        // find attachment ids
        val ids = json.optJSONArrayOrJSONObject("data")
            .mapNotNull { it.attributes }.firstOrNull { it.optString("abbreviation") == tool }
            ?.let { attrs -> attachments.mapNotNull { attrs.optString(it) } }?.mapNotNull { it.toIntOrNull() }
            .orEmpty()

        // generate filenames for each attachment id
        val included = json.optJSONArrayOrJSONObject("included").filter { it.jsonApiType == "attachment" }
            .associateBy { it.jsonApiId }
        val attachments = buildMap<String, String> {
            ids.forEach { id ->
                included[id]?.generateFilename()?.let { put("attachments/$id/download", it) }
            }
        }

        // write the list of attachments to an intermediate json file
        JSONObject(attachments).writeJson(output.asFile.get())
    }

    private fun JSONObject.generateFilename(): String? {
        val rawName = attributes?.optString("file-file-name", null) ?: return null
        val sha256 = attributes?.optString("sha256", null) ?: return null
        return sha256 + rawName.substring(rawName.lastIndexOf('.'))
    }
}
