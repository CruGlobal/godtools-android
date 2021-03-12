package org.cru.godtools.xml.model

import android.net.Uri
import androidx.annotation.ColorInt
import androidx.annotation.RestrictTo
import org.cru.godtools.base.model.Event
import org.xmlpull.v1.XmlPullParser

private const val XML_COLOR = "color"
private const val XML_TYPE = "type"
private const val XML_TYPE_EVENT = "event"
private const val XML_TYPE_URL = "url"
private const val XML_STYLE = "style"
private const val XML_STYLE_CONTAINED = "contained"
private const val XML_STYLE_OUTLINED = "outlined"
private const val XML_URL = "url"

class Button : Content, Styles {
    companion object {
        internal const val XML_BUTTON = "button"
    }

    enum class Type {
        EVENT, URL, UNKNOWN;

        companion object {
            internal val DEFAULT = UNKNOWN

            internal fun parseOrNull(value: String?) = when (value) {
                XML_TYPE_EVENT -> EVENT
                XML_TYPE_URL -> URL
                else -> null
            }
        }
    }

    enum class Style { CONTAINED, OUTLINED, UNKNOWN }
    private fun String?.parseStyleOrNull() = when (this) {
        null -> null
        XML_STYLE_CONTAINED -> Style.CONTAINED
        XML_STYLE_OUTLINED -> Style.OUTLINED
        else -> Style.UNKNOWN
    }

    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_BUTTON)

        type = Type.parseOrNull(parser.getAttributeValue(null, XML_TYPE)) ?: Type.DEFAULT
        events = parseEvents(parser, XML_EVENTS)
        url = parser.getAttributeValueAsUriOrNull(XML_URL)

        _style = parser.getAttributeValue(null, XML_STYLE).parseStyleOrNull()
        _buttonColor = parser.getAttributeValueAsColorOrNull(XML_COLOR)

        // process any child elements
        val analyticsEvents = mutableListOf<AnalyticsEvent>()
        text = parser.parseTextChild(this, XMLNS_CONTENT, XML_BUTTON) {
            when {
                parser.namespace == XMLNS_ANALYTICS && parser.name == AnalyticsEvent.XML_EVENTS ->
                    analyticsEvents += AnalyticsEvent.fromEventsXml(this, parser)
            }
        }
        this.analyticsEvents = analyticsEvents
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    internal constructor(
        parent: Base,
        type: Type = Type.DEFAULT,
        style: Style? = null,
        @ColorInt color: Int? = null,
        text: ((Button) -> Text?)? = null
    ) : super(parent) {
        this.type = type
        events = emptySet()
        url = null

        _style = style
        _buttonColor = color

        analyticsEvents = emptySet()
        this.text = text?.invoke(this)
    }

    val type: Type
    val events: Set<Event.Id>
    val url: Uri?

    private val _style: Style?
    val style: Style get() = _style ?: stylesParent.buttonStyle

    @ColorInt
    private val _buttonColor: Int?
    @get:ColorInt
    override val buttonColor: Int get() = _buttonColor ?: stylesParent.let { it?.buttonColor ?: it.primaryColor }

    val text: Text?
    override val textAlign get() = Text.Align.CENTER
    override val textColor get() = stylesParent.primaryTextColor

    val analyticsEvents: Collection<AnalyticsEvent>

    override val isIgnored get() = super.isIgnored || type == Type.UNKNOWN || style == Style.UNKNOWN
}

val Button?.buttonColor get() = this?.buttonColor ?: stylesParent.primaryColor
val Button?.textColor get() = this?.textColor ?: stylesParent.primaryTextColor
