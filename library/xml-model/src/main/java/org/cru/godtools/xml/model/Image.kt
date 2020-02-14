package org.cru.godtools.xml.model

import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import org.ccci.gto.android.common.util.XmlPullParserUtils
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.Constants
import org.xmlpull.v1.XmlPullParser

private const val XML_RESOURCE = "resource"

class Image : Content {
    @WorkerThread
    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, Constants.XMLNS_CONTENT, XML_IMAGE)

        resourceName = parser.getAttributeValue(null, XML_RESOURCE)
        events = parseEvents(parser, Base.XML_EVENTS)

        XmlPullParserUtils.skipTag(parser)
    }

    val events: Set<Event.Id>
    @VisibleForTesting
    internal val resourceName: String?
    val resource: Resource? get() = getResource(resourceName)

    companion object {
        const val XML_IMAGE = "image"

        @JvmStatic
        @Deprecated("Use Image?.resource extension value instead", ReplaceWith("image.resource"))
        fun getResource(image: Image?) = image.resource

        @JvmStatic
        @WorkerThread
        @Deprecated(
            "Use Image constructor instead",
            ReplaceWith("Image(parent, parser)", "org.cru.godtools.xml.model.Image")
        )
        fun fromXml(parent: Base, parser: XmlPullParser) = Image(parent, parser)
    }
}

val Image?.resource get() = this?.resource
