package org.cru.godtools.xml.model

import org.ccci.gto.android.common.util.XmlPullParserUtils
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.Constants
import org.cru.godtools.xml.model.Content.Companion.fromXml
import org.xmlpull.v1.XmlPullParser
import java.util.Collections

private const val XML_LABEL = "label"

class Tab internal constructor(parent: Tabs, val position: Int, parser: XmlPullParser) : Base(parent), Parent {
    companion object {
        internal const val XML_TAB = "tab"
    }

    val analyticsEvents: Collection<AnalyticsEvent>
    val listeners: Set<Event.Id>
    val label: Text?
    override val content: List<Content>

    init {
        parser.require(XmlPullParser.START_TAG, Constants.XMLNS_CONTENT, XML_TAB)
        listeners = parseEvents(parser, XML_LISTENERS)

        // process any child elements
        var analyticsEvents: Collection<AnalyticsEvent> = emptyList()
        var label: Text? = null
        val contentList = mutableListOf<Content>()
        parsingChildren@ while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                Constants.XMLNS_ANALYTICS -> when (parser.name) {
                    AnalyticsEvent.XML_EVENTS -> {
                        analyticsEvents = AnalyticsEvent.fromEventsXml(parser)
                        continue@parsingChildren
                    }
                }
                Constants.XMLNS_CONTENT -> when (parser.name) {
                    XML_LABEL -> {
                        label = Text.fromNestedXml(this, parser, Constants.XMLNS_CONTENT, XML_LABEL)
                        continue@parsingChildren
                    }
                }
            }

            // try parsing this child element as a content node
            val content = fromXml(this, parser)
            if (content != null) {
                if (!content.isIgnored) contentList.add(content)
                continue
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser)
        }

        this.analyticsEvents = analyticsEvents
        this.label = label
        content = Collections.unmodifiableList(contentList)
    }
}
