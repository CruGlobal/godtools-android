package org.cru.godtools.api.model

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType

@JsonApiType("publisher-info")
class PublisherInfo(
    var subscriberChannelId: String? = null
)
