package org.cru.godtools.xml.model

import org.ccci.gto.android.common.util.XmlPullParserUtils
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.XMLNS_ANALYTICS
import org.cru.godtools.xml.XMLNS_CONTENT
import org.xmlpull.v1.XmlPullParser

class Link internal constructor(parent: BaseModel, parser: XmlPullParser) : Content(parent, parser) {
    companion object {
        internal const val XML_LINK = "link"
    }

    val analyticsEvents: Collection<AnalyticsEvent>
    val events: Set<Event.Id>
    val text: Text?

    init {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_LINK)
        events = parseEvents(parser, XML_EVENTS)

        // process any child elements
        var analyticsEvents: Collection<AnalyticsEvent> = emptyList()
        var text: Text? = null
        parsingChildren@ while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                XMLNS_ANALYTICS -> when (parser.name) {
                    AnalyticsEvent.XML_EVENTS -> {
                        analyticsEvents = AnalyticsEvent.fromEventsXml(parser)
                        continue@parsingChildren
                    }
                }
                XMLNS_CONTENT -> when (parser.name) {
                    Text.XML_TEXT -> {
                        text = Text(this, parser)
                        continue@parsingChildren
                    }
                }
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser)
        }
        this.analyticsEvents = analyticsEvents
        this.text = text
    }
}
