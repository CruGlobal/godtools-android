package org.cru.godtools.xml.model

import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.tool.model.EventId
import org.xmlpull.v1.XmlPullParser

private const val XML_RESOURCE = "resource"
private const val XML_LOOP = "loop"
private const val XML_AUTOPLAY = "autoplay"
private const val XML_PLAY_LISTENERS = "play-listeners"
private const val XML_STOP_LISTENERS = "stop-listeners"

class Animation : Content {
    companion object {
        internal const val XML_ANIMATION = "animation"
    }

    @VisibleForTesting
    internal val resourceName: String?
    val resource get() = getResource(resourceName)

    val loop: Boolean
    val autoPlay: Boolean

    val events: Set<EventId>
    val playListeners: Set<EventId>
    val stopListeners: Set<EventId>

    @WorkerThread
    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_ANIMATION)

        resourceName = parser.getAttributeValue(null, XML_RESOURCE)
        loop = parser.getAttributeValue(null, XML_LOOP)?.toBoolean() ?: true
        autoPlay = parser.getAttributeValue(null, XML_AUTOPLAY)?.toBoolean() ?: true

        events = parseEvents(parser, XML_EVENTS)
        playListeners = parseEvents(parser, XML_PLAY_LISTENERS)
        stopListeners = parseEvents(parser, XML_STOP_LISTENERS)

        parser.skipTag()
    }
}
