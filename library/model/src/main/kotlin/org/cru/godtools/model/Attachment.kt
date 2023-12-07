package org.cru.godtools.model

import androidx.annotation.RestrictTo
import kotlin.random.Random
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType
import org.cru.godtools.base.FileSystem

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
        get() = field?.takeUnless { it == INVALID_ID } ?: tool?.apiId ?: INVALID_ID
    var toolCode: String? = null
        get() = field ?: tool?.code

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

    suspend fun getFile(fs: FileSystem) = localFilename?.let { fs.file(it) }
}

// TODO: move this to testFixtures once they support Kotlin source files
@RestrictTo(RestrictTo.Scope.TESTS)
@Suppress("ktlint:standard:function-naming")
fun Attachment(
    id: Long = Random.nextLong(),
    tool: Tool? = null,
    toolId: Long? = tool?.apiId,
    toolCode: String? = tool?.code,
    block: Attachment.() -> Unit = {},
) = Attachment().apply {
    this.id = id
    this.toolId = toolId
    this.toolCode = toolCode
    block()
}
