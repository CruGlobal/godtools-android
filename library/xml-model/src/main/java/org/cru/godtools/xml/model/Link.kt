package org.cru.godtools.xml.model

import org.cru.godtools.base.model.Event
import org.xmlpull.v1.XmlPullParser

class Link internal constructor(parent: Base, parser: XmlPullParser) : Content(parent, parser) {
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
        val analyticsEvents = mutableListOf<AnalyticsEvent>()
        text = parser.parseTextChild(this, XMLNS_CONTENT, XML_LINK) {
            when {
                parser.namespace == XMLNS_ANALYTICS && parser.name == AnalyticsEvent.XML_EVENTS ->
                    analyticsEvents += AnalyticsEvent.fromEventsXml(this, parser)
            }
        }
        this.analyticsEvents = analyticsEvents
    }
}
