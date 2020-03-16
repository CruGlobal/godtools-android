package org.cru.godtools.xml.model

import androidx.annotation.ColorInt
import org.ccci.gto.android.common.util.XmlPullParserUtils
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.XMLNS_CONTENT
import org.cru.godtools.xml.XMLNS_TRACT
import org.xmlpull.v1.XmlPullParser

private const val XML_CONTROL_COLOR = "control-color"

class CallToAction : Base {
    companion object {
        // TODO: make this internal
        const val XML_CALL_TO_ACTION = "call-to-action"
    }

    val label: Text?
    val events: Set<Event.Id>
    @ColorInt
    private val _controlColor: Int?
    @get:ColorInt
    val controlColor get() = _controlColor ?: Styles.getPrimaryColor(page)

    internal constructor(parent: Base) : super(parent) {
        label = null
        events = emptySet()
        _controlColor = null
    }

    internal constructor(parent: Base, parser: XmlPullParser) : super(parent) {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_CALL_TO_ACTION)

        events = parseEvents(parser, XML_EVENTS)
        _controlColor = parser.getAttributeValueAsColorOrNull(XML_CONTROL_COLOR)

        // process any child elements
        var label: Text? = null
        parsingChildren@ while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                XMLNS_CONTENT -> when (parser.name) {
                    Text.XML_TEXT -> {
                        label = Text(this, parser)
                        continue@parsingChildren
                    }
                }
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser)
        }
        this.label = label
    }
}

@get:ColorInt
val CallToAction?.controlColor: Int get() = this?.controlColor ?: Styles.getPrimaryColor(null)
