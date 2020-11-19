package org.cru.godtools.xml.model

import androidx.annotation.DimenRes
import androidx.annotation.RestrictTo
import org.cru.godtools.xml.R
import org.cru.godtools.xml.XMLNS_ANALYTICS
import org.cru.godtools.xml.XMLNS_TRACT
import org.xmlpull.v1.XmlPullParser

private const val XML_HEADING = "heading"

class Hero : BaseModel, Parent, Styles {
    companion object {
        internal const val XML_HERO = "hero"
    }

    val analyticsEvents: Collection<AnalyticsEvent>
    val heading: Text?
    override val content: List<Content>

    @get:DimenRes
    override val textSize get() = R.dimen.text_size_hero

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(parent: Base, analyticsEvents: Collection<AnalyticsEvent> = emptyList()) : super(parent) {
        this.analyticsEvents = analyticsEvents
        heading = null
        content = emptyList()
    }

    internal constructor(parent: Base, parser: XmlPullParser) : super(parent) {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_HERO)

        // process any child elements
        var analyticsEvents: Collection<AnalyticsEvent> = emptyList()
        var heading: Text? = null
        content = parseContent(parser) {
            when (parser.namespace) {
                XMLNS_ANALYTICS -> when (parser.name) {
                    AnalyticsEvent.XML_EVENTS -> analyticsEvents = AnalyticsEvent.fromEventsXml(parser)
                }
                XMLNS_TRACT -> when (parser.name) {
                    XML_HEADING -> heading = Text.fromNestedXml(this, parser, XMLNS_TRACT, XML_HEADING)
                }
            }
        }
        this.analyticsEvents = analyticsEvents
        this.heading = heading
    }
}
