package org.cru.godtools.model

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiId
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType

private const val JSON_API_TYPE = "user-counter"

private const val JSON_COUNT = "count"
private const val JSON_DECAYED_COUNT = "decayed-count"
private const val JSON_INCREMENT = "increment"

@JsonApiType(JSON_API_TYPE)
class UserCounter @JvmOverloads constructor(@JsonApiId val id: String = "") {
    @JsonApiAttribute(JSON_INCREMENT, deserialize = false)
    var delta: Int = 0

    @JsonApiAttribute(JSON_COUNT, serialize = false)
    var apiCount: Int = 0
    val count get() = apiCount + (delta ?: 0)

    @JsonApiAttribute(JSON_DECAYED_COUNT, serialize = false)
    var apiDecayedCount: Double = 0.0
    val decayedCount get() = apiDecayedCount + (delta ?: 0)
}
