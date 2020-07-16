package org.cru.godtools.xml.model

import androidx.annotation.ColorInt
import androidx.annotation.RestrictTo
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.XMLNS_TRACT
import org.xmlpull.v1.XmlPullParser

private const val XML_CONTROL_COLOR = "control-color"

class CallToAction : BaseObj {
    companion object {
        // TODO: make this internal
        const val XML_CALL_TO_ACTION = "call-to-action"
    }

    val label: Text?
    val events: Set<Event.Id>
    @ColorInt
    private val _controlColor: Int?
    @get:ColorInt
    val controlColor get() = _controlColor ?: page.primaryColor

    internal constructor(parent: Base) : super(parent) {
        label = null
        events = emptySet()
        _controlColor = null
    }

    internal constructor(parent: Base, parser: XmlPullParser) : super(parent) {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_CALL_TO_ACTION)

        events = parseEvents(parser, XML_EVENTS)
        _controlColor = parser.getAttributeValueAsColorOrNull(XML_CONTROL_COLOR)

        label = Text.fromNestedXml(this, parser, XMLNS_TRACT, XML_CALL_TO_ACTION)
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(
        parent: Base,
        label: ((CallToAction) -> Text?)? = null,
        events: Set<Event.Id> = emptySet(),
        @ColorInt controlColor: Int? = null
    ) : super(parent) {
        this.label = label?.invoke(this)
        this.events = events
        _controlColor = controlColor
    }
}

@get:ColorInt
val CallToAction?.controlColor: Int get() = this?.controlColor ?: stylesParent.primaryColor
