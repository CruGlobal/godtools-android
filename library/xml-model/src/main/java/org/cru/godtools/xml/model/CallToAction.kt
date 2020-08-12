package org.cru.godtools.xml.model

import androidx.annotation.ColorInt
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.XMLNS_TRACT
import org.cru.godtools.xml.XMLNS_TRAINING
import org.xmlpull.v1.XmlPullParser

private const val XML_CONTROL_COLOR = "control-color"
private const val XML_TIP = "tip"

class CallToAction : BaseModel {
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

    @VisibleForTesting
    internal val tipId: String?
    val tip get() = manifest.findTip(tipId)

    internal constructor(parent: Base) : super(parent) {
        label = null
        events = emptySet()
        _controlColor = null
        tipId = null
    }

    internal constructor(parent: Base, parser: XmlPullParser) : super(parent) {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_CALL_TO_ACTION)

        events = parseEvents(parser, XML_EVENTS)
        _controlColor = parser.getAttributeValueAsColorOrNull(XML_CONTROL_COLOR)
        tipId = parser.getAttributeValue(XMLNS_TRAINING, XML_TIP)

        label = Text.fromNestedXml(this, parser, XMLNS_TRACT, XML_CALL_TO_ACTION)
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(
        parent: Base,
        label: ((CallToAction) -> Text?)? = null,
        events: Set<Event.Id> = emptySet(),
        @ColorInt controlColor: Int? = null,
        tip: String? = null
    ) : super(parent) {
        this.label = label?.invoke(this)
        this.events = events
        _controlColor = controlColor
        tipId = tip
    }
}

@get:ColorInt
val CallToAction?.controlColor: Int get() = this?.controlColor ?: stylesParent.primaryColor
