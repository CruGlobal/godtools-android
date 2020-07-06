package org.cru.godtools.xml.model

import androidx.annotation.ColorInt
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.XMLNS_ANALYTICS
import org.cru.godtools.xml.XMLNS_TRACT
import org.xmlpull.v1.XmlPullParser

private const val XML_LABEL = "label"
private const val XML_HIDDEN = "hidden"
private val DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ImageScaleType.FILL_X
private val DEFAULT_BACKGROUND_IMAGE_GRAVITY = ImageGravity.CENTER

class Card : Base, Styles, Parent {
    companion object {
        internal const val XML_CARD = "card"
    }

    val id get() = "${page.id}-$position"
    val position: Int

    val isHidden: Boolean
    val listeners: Set<Event.Id>
    val dismissListeners: Set<Event.Id>
    val analyticsEvents: Collection<AnalyticsEvent>

    private val _backgroundImage: String?
    val backgroundImage get() = getResource(_backgroundImage)
    internal val backgroundImageGravity: ImageGravity
    val backgroundImageScaleType: ImageScaleType

    @ColorInt
    private val _textColor: Int?
    @get:ColorInt
    override val textColor get() = _textColor ?: page.cardTextColor

    // TODO: implement card-background-color on Page & Manifest
    @ColorInt
    private val _backgroundColor: Int?
    @get:ColorInt
    val backgroundColor get() = _backgroundColor ?: manifest.backgroundColor

    val label: Text?
    override val content: List<Content>

    internal constructor(parent: Page, position: Int, parser: XmlPullParser) : super(parent) {
        this.position = position

        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_CARD)

        isHidden = parser.getAttributeValue(null, XML_HIDDEN)?.toBoolean() ?: false
        listeners = parseEvents(parser, XML_LISTENERS)
        dismissListeners = parseEvents(parser, XML_DISMISS_LISTENERS)

        _backgroundImage = parser.getAttributeValue(null, XML_BACKGROUND_IMAGE)
        backgroundImageGravity =
            parser.getAttributeValueAsImageGravity(XML_BACKGROUND_IMAGE_GRAVITY, DEFAULT_BACKGROUND_IMAGE_GRAVITY)
        backgroundImageScaleType = parser.getAttributeValueAsImageScaleTypeOrNull(XML_BACKGROUND_IMAGE_SCALE_TYPE)
            ?: DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE

        _textColor = parser.getAttributeValueAsColorOrNull(XML_TEXT_COLOR)
        _backgroundColor = parser.getAttributeValueAsColorOrNull(XML_BACKGROUND_COLOR)

        // process any child elements
        var analyticsEvents: Collection<AnalyticsEvent>? = null
        var label: Text? = null
        content = parseContent(parser) {
            when (parser.namespace) {
                XMLNS_ANALYTICS -> when (parser.name) {
                    AnalyticsEvent.XML_EVENTS -> analyticsEvents = AnalyticsEvent.fromEventsXml(parser)
                }
                XMLNS_TRACT -> when (parser.name) {
                    XML_LABEL -> label = Text.fromNestedXml(this@Card, parser, XMLNS_TRACT, XML_LABEL)
                }
            }
        }
        this.analyticsEvents = analyticsEvents.orEmpty()
        this.label = label
    }
}

// TODO: implement card-background-color on Page & Manifest
val Card?.backgroundColor get() = this?.backgroundColor ?: (null as Manifest?).backgroundColor
val Card?.backgroundImageGravity get() = this?.backgroundImageGravity ?: DEFAULT_BACKGROUND_IMAGE_GRAVITY
val Card?.backgroundImageScaleType get() = this?.backgroundImageScaleType ?: DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE
