package org.cru.godtools.xml.model.tract

import androidx.annotation.ColorInt
import androidx.annotation.RestrictTo
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.model.AnalyticsEvent
import org.cru.godtools.xml.model.BaseModel
import org.cru.godtools.xml.model.Content
import org.cru.godtools.xml.model.ImageGravity
import org.cru.godtools.xml.model.ImageScaleType
import org.cru.godtools.xml.model.Parent
import org.cru.godtools.xml.model.Styles
import org.cru.godtools.xml.model.Text
import org.cru.godtools.xml.model.XMLNS_ANALYTICS
import org.cru.godtools.xml.model.XMLNS_TRACT
import org.cru.godtools.xml.model.XML_BACKGROUND_COLOR
import org.cru.godtools.xml.model.XML_BACKGROUND_IMAGE
import org.cru.godtools.xml.model.XML_BACKGROUND_IMAGE_GRAVITY
import org.cru.godtools.xml.model.XML_BACKGROUND_IMAGE_SCALE_TYPE
import org.cru.godtools.xml.model.XML_DISMISS_LISTENERS
import org.cru.godtools.xml.model.XML_LISTENERS
import org.cru.godtools.xml.model.XML_TEXT_COLOR
import org.cru.godtools.xml.model.backgroundColor
import org.cru.godtools.xml.model.contentTips
import org.cru.godtools.xml.model.getAttributeValueAsColorOrNull
import org.cru.godtools.xml.model.getAttributeValueAsImageGravity
import org.cru.godtools.xml.model.getAttributeValueAsImageScaleTypeOrNull
import org.cru.godtools.xml.model.parseContent
import org.cru.godtools.xml.model.parseTextChild
import org.xmlpull.v1.XmlPullParser

private const val XML_LABEL = "label"
private const val XML_HIDDEN = "hidden"
private val DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ImageScaleType.FILL_X
private val DEFAULT_BACKGROUND_IMAGE_GRAVITY = ImageGravity.CENTER

class Card : BaseModel, Styles, Parent {
    companion object {
        internal const val XML_CARD = "card"
    }

    val page: TractPage

    val id get() = "${page.id}-$position"
    val position: Int
    val visiblePosition get() = page.visibleCards.indexOf(this).takeUnless { it == -1 }
    val isLastVisibleCard get() = this == page.visibleCards.lastOrNull()

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

    @ColorInt
    private val _backgroundColor: Int?
    @get:ColorInt
    internal val backgroundColor get() = _backgroundColor ?: page.cardBackgroundColor

    val label: Text?
    override val content: List<Content>
    val tips get() = contentTips

    internal constructor(parent: TractPage, position: Int, parser: XmlPullParser) : super(parent) {
        page = parent
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
                    AnalyticsEvent.XML_EVENTS -> analyticsEvents = AnalyticsEvent.fromEventsXml(this, parser)
                }
                XMLNS_TRACT -> when (parser.name) {
                    XML_LABEL -> label = parser.parseTextChild(this@Card, XMLNS_TRACT, XML_LABEL)
                }
            }
        }
        this.analyticsEvents = analyticsEvents.orEmpty()
        this.label = label
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(
        parent: TractPage,
        position: Int = 0,
        backgroundColor: Int? = null,
        isHidden: Boolean = false,
        content: ((Card) -> List<Content>?)? = null
    ) : super(parent) {
        page = parent
        this.position = position

        this.isHidden = isHidden
        listeners = emptySet()
        dismissListeners = emptySet()
        analyticsEvents = emptySet()

        _backgroundColor = backgroundColor
        _backgroundImage = null
        backgroundImageGravity = DEFAULT_BACKGROUND_IMAGE_GRAVITY
        backgroundImageScaleType = DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE

        _textColor = null

        label = null
        this.content = content?.invoke(this).orEmpty()
    }
}

@get:ColorInt
val Card?.backgroundColor get() = this?.backgroundColor ?: this?.manifest.backgroundColor
val Card?.backgroundImageGravity get() = this?.backgroundImageGravity ?: DEFAULT_BACKGROUND_IMAGE_GRAVITY
val Card?.backgroundImageScaleType get() = this?.backgroundImageScaleType ?: DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE
