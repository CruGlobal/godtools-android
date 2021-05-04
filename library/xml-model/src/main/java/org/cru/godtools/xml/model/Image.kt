package org.cru.godtools.xml.model

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.xmlpull.v1.XmlPullParser

private const val XML_RESOURCE = "resource"

class Image : Content {
    companion object {
        internal const val XML_IMAGE = "image"
    }

    val events: Set<Event.Id>
    @VisibleForTesting
    internal val resourceName: String?
    val resource get() = getResource(resourceName)

    @WorkerThread
    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_IMAGE)

        resourceName = parser.getAttributeValue(null, XML_RESOURCE)
        events = parseEvents(parser, XML_EVENTS)

        parser.skipTag()
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(parent: Base) : super(parent) {
        events = emptySet()
        resourceName = null
    }
}
