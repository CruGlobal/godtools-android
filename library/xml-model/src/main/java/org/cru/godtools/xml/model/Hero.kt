package org.cru.godtools.xml.model

import androidx.annotation.DimenRes
import org.ccci.gto.android.common.util.XmlPullParserUtils
import org.cru.godtools.xml.R
import org.cru.godtools.xml.XMLNS_ANALYTICS
import org.cru.godtools.xml.XMLNS_TRACT
import org.xmlpull.v1.XmlPullParser
import java.util.Collections

private const val XML_HEADING = "heading"

class Hero internal constructor(parent: Base, parser: XmlPullParser) : Base(parent), Parent, Styles {
    val analyticsEvents: Collection<AnalyticsEvent>
    val heading: Text?
    override val content: List<Content>

    @DimenRes
    override fun getTextSize() = R.dimen.text_size_hero

    init {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_HERO)

        // process any child elements
        var analyticsEvents: Collection<AnalyticsEvent> = emptyList()
        var heading: Text? = null
        val contentList = mutableListOf<Content>()
        parsingChildren@ while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                XMLNS_ANALYTICS -> when (parser.name) {
                    AnalyticsEvent.XML_EVENTS -> {
                        analyticsEvents = AnalyticsEvent.fromEventsXml(parser)
                        continue@parsingChildren
                    }
                }
                XMLNS_TRACT -> when (parser.name) {
                    XML_HEADING -> {
                        heading = Text.fromNestedXml(this, parser, XMLNS_TRACT, XML_HEADING)
                        continue@parsingChildren
                    }
                }
            }

            // try parsing this child element as a content node
            val content = Content.fromXml(this, parser)
            if (content != null) {
                if (!content.isIgnored) contentList.add(content)
                continue@parsingChildren
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser)
        }
        this.analyticsEvents = analyticsEvents
        this.heading = heading
        content = Collections.unmodifiableList(contentList)
    }

    companion object {
        // TODO: make this internal
        const val XML_HERO = "hero"

        @JvmStatic
        @Deprecated("Use constructor instead", ReplaceWith("Hero(parent, parser)", "org.cru.godtools.xml.model.Hero"))
        fun fromXml(parent: Base, parser: XmlPullParser) = Hero(parent, parser)
    }
}
