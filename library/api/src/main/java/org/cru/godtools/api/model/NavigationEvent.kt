package org.cru.godtools.api.model

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType
import java.util.Locale

@JsonApiType("navigation-event")
class NavigationEvent(
    var tool: String? = null,
    var locale: Locale? = null,
    var page: Int? = null,
    var card: Int? = null
)
