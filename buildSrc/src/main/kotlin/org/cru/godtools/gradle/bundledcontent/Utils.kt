package org.cru.godtools.gradle.bundledcontent

import java.io.File
import org.json.JSONObject
import org.json.JSONTokener

internal fun File.loadJson() = inputStream().use { JSONObject(JSONTokener(it)) }
internal fun JSONObject.writeJson(file: File) = file.writer().use { write(it) }

internal fun JSONObject.optJSONArrayOrJSONObject(key: String) =
    (optJSONArray(key)?.filterIsInstance<JSONObject>() ?: listOfNotNull(optJSONObject("data")))
internal val JSONObject.jsonApiType get() = optString("type", null)
internal val JSONObject.jsonApiId get() = optString("id", null)?.toIntOrNull()
internal val JSONObject.attributes get() = optJSONObject("attributes")
internal val JSONObject.relationships get() = optJSONObject("relationships")
