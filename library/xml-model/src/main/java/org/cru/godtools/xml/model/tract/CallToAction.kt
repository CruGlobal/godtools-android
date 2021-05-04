package org.cru.godtools.xml.model.tract

import androidx.annotation.ColorInt
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import org.cru.godtools.xml.model.BaseModel
import org.cru.godtools.xml.model.Event
import org.cru.godtools.xml.model.Text
import org.cru.godtools.xml.model.XMLNS_TRACT
import org.cru.godtools.xml.model.XMLNS_TRAINING
import org.cru.godtools.xml.model.XML_EVENTS
import org.cru.godtools.xml.model.getAttributeValueAsColorOrNull
import org.cru.godtools.xml.model.parseTextChild
import org.cru.godtools.xml.model.primaryColor
import org.cru.godtools.xml.model.stylesParent
import org.xmlpull.v1.XmlPullParser

private const val XML_CONTROL_COLOR = "control-color"
private const val XML_TIP = "tip"

class CallToAction : BaseModel {
    companion object {
        internal const val XML_CALL_TO_ACTION = "call-to-action"
    }

    private val page: TractPage

    val label: Text?
    val events: Set<Event.Id>
    @ColorInt
    private val _controlColor: Int?
    @get:ColorInt
    val controlColor get() = _controlColor ?: page.primaryColor

    @VisibleForTesting
    internal val tipId: String?
    val tip get() = manifest.findTip(tipId)

    internal constructor(parent: TractPage) : super(parent) {
        page = parent
        label = null
        events = emptySet()
        _controlColor = null
        tipId = null
    }

    internal constructor(parent: TractPage, parser: XmlPullParser) : super(parent) {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_CALL_TO_ACTION)

        page = parent
        events = parseEvents(parser, XML_EVENTS)
        _controlColor = parser.getAttributeValueAsColorOrNull(XML_CONTROL_COLOR)
        tipId = parser.getAttributeValue(XMLNS_TRAINING, XML_TIP)

        label = parser.parseTextChild(this, XMLNS_TRACT, XML_CALL_TO_ACTION)
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(
        parent: TractPage,
        label: ((CallToAction) -> Text?)? = null,
        events: Set<Event.Id> = emptySet(),
        @ColorInt controlColor: Int? = null,
        tip: String? = null
    ) : super(parent) {
        page = parent
        this.label = label?.invoke(this)
        this.events = events
        _controlColor = controlColor
        tipId = tip
    }
}

@get:ColorInt
val CallToAction?.controlColor: Int get() = this?.controlColor ?: stylesParent.primaryColor
