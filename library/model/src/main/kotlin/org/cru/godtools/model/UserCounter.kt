package org.cru.godtools.model

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiId
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType

private const val JSON_API_TYPE = "user-counter"

private const val JSON_COUNT = "count"
private const val JSON_DECAYED_COUNT = "decayed-count"
private const val JSON_INCREMENT = "increment"

@JsonApiType(JSON_API_TYPE)
data class UserCounter(
    @JsonApiId
    val name: String = "",
    @JsonApiAttribute(JSON_COUNT, serialize = false)
    val apiCount: Int = 0,
    @JsonApiAttribute(JSON_DECAYED_COUNT, serialize = false)
    val apiDecayedCount: Double = 0.0,
    @JsonApiAttribute(JSON_INCREMENT, deserialize = false)
    val delta: Int = 0
) {
    internal constructor() : this("")

    companion object {
        val VALID_NAME = Regex("[a-zA-Z0-9_\\-.]+")
    }

    val count get() = apiCount + delta
    val decayedCount get() = apiDecayedCount + delta
}
