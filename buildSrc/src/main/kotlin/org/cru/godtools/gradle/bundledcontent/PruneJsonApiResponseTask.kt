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
import org.json.JSONArray
import org.json.JSONObject

@CacheableTask
abstract class PruneJsonApiResponseTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val input: RegularFileProperty

    @get:Input
    var removeAttributesFor = mutableMapOf<String, List<String>>()
    @get:Input
    var removeAllRelationshipsFor = emptyList<String>()

    @get:Input
    var sortData = true
    @get:Input
    var sortRelationships = true

    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    fun pruneJsonApi() {
        val json = input.asFile.get().loadJson()

        // remove attributes/relationships for all data and included objects
        (json.optJSONArrayOrJSONObject("data") + json.optJSONArrayOrJSONObject("included")).forEach {
            if (it.jsonApiType in removeAllRelationshipsFor) it.remove("relationships")
            it.removeJsonApiAttributes()
        }

        // sort data and included
        if (sortData) json.optJSONArray("data")?.sortJsonApiObjects()
        json.optJSONArray("included")?.sortJsonApiObjects()

        json.writeJson(output.asFile.get())
    }

    private fun JSONObject.removeJsonApiAttributes() {
        val attributes = optJSONObject("attributes")
        val relationships = optJSONObject("relationships")
        removeAttributesFor[jsonApiType]?.forEach {
            attributes?.remove(it)
            relationships?.remove(it)
        }
    }

    private fun JSONArray.sortJsonApiObjects() {
        val sorted = filterIsInstance<JSONObject>()
            .onEach { if (sortRelationships) it.sortJsonApiRelationships() }
            .sortedBy { it.optInt("id") }.sortedBy { it.getString("type") }
        clear()
        putAll(sorted)
    }

    private fun JSONObject.sortJsonApiRelationships() {
        optJSONObject("relationships")?.let { relationships ->
            relationships.keySet().forEach {
                relationships.optJSONObject(it)?.optJSONArray("data")?.sortJsonApiObjects()
            }
        }
    }
}
