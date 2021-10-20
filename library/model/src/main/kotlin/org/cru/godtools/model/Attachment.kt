package org.cru.godtools.model

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType
import org.cru.godtools.base.FileManager

private const val JSON_API_TYPE = "attachment"
private const val JSON_RESOURCE = "resource"
private const val JSON_FILE_NAME = "file-file-name"
private const val JSON_SHA256 = "sha256"

@JsonApiType(JSON_API_TYPE)
class Attachment : Base() {
    @JsonApiAttribute(JSON_RESOURCE)
    private val tool: Tool? = null
    @JsonApiIgnore
    var toolId: Long? = null
        get() = field?.takeUnless { it == INVALID_ID } ?: tool?.id ?: INVALID_ID

    @JsonApiAttribute(JSON_FILE_NAME)
    var filename: String? = null
    @JsonApiAttribute(JSON_SHA256)
    var sha256: String? = null

    @JsonApiIgnore
    var isDownloaded = false

    val localFilename: String?
        get() = sha256?.let { sha256 ->
            val extension = filename?.substringAfterLast('.', "bin") ?: "bin"
            "$sha256.$extension"
        }

    suspend fun getFile(manager: FileManager) = localFilename?.let { manager.getFile(it) }
    fun getFileBlocking(manager: FileManager) = localFilename?.let { manager.getFileBlocking(it) }
}
