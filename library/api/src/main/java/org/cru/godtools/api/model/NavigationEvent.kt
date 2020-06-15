package org.cru.godtools.api.model

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiId
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType
import java.util.Locale
import java.util.UUID

@JsonApiType("navigation-event")
data class NavigationEvent(
    var tool: String? = null,
    var locale: Locale? = null,
    var page: Int? = null,
    var card: Int? = null
) {
    @JsonApiId
    private var id = UUID.randomUUID().toString()
}
