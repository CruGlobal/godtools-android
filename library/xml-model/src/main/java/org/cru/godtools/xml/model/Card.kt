package org.cru.godtools.xml.model

import androidx.annotation.ColorInt
import org.ccci.gto.android.common.util.XmlPullParserUtils
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.XMLNS_ANALYTICS
import org.cru.godtools.xml.XMLNS_TRACT
import org.cru.godtools.xml.model.Text.Companion.fromNestedXml
import org.xmlpull.v1.XmlPullParser

private const val XML_LABEL = "label"
private const val XML_HIDDEN = "hidden"
private val DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ImageScaleType.FILL_X
private const val DEFAULT_BACKGROUND_IMAGE_GRAVITY = CENTER

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

    val backgroundImage: String?
    val backgroundImageGravity: Int
    val backgroundImageScaleType: ImageScaleType

    @ColorInt
    private val _textColor: Int?
    @get:ColorInt
    override val textColor get() = _textColor ?: page.cardTextColor

    // TODO: implement card-background-color on Page & Manifest
    @ColorInt
    private val _backgroundColor: Int?
    @get:ColorInt
    val backgroundColor get() = _backgroundColor ?: Manifest.getBackgroundColor(manifest)

    val label: Text?
    override val content: List<Content>

    @OptIn(ExperimentalStdlibApi::class)
    internal constructor(parent: Page, position: Int, parser: XmlPullParser) : super(parent) {
        this.position = position

        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_CARD)

        isHidden = parser.getAttributeValue(null, XML_HIDDEN)?.toBoolean() ?: false
        listeners = parseEvents(parser, XML_LISTENERS)
        dismissListeners = parseEvents(parser, XML_DISMISS_LISTENERS)

        backgroundImage = parser.getAttributeValue(null, XML_BACKGROUND_IMAGE)
        backgroundImageGravity =
            parser.parseImageGravity(XML_BACKGROUND_IMAGE_GRAVITY, DEFAULT_BACKGROUND_IMAGE_GRAVITY)
        backgroundImageScaleType = parser.getAttributeValueAsImageScaleTypeOrNull(XML_BACKGROUND_IMAGE_SCALE_TYPE)
            ?: DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE

        _textColor = parser.getAttributeValueAsColorOrNull(XML_TEXT_COLOR)
        _backgroundColor = parser.getAttributeValueAsColorOrNull(XML_BACKGROUND_COLOR)

        // process any child elements
        var analyticsEvents: Collection<AnalyticsEvent>? = null
        var label: Text? = null
        content = buildList<Content> {
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
                        XML_LABEL -> {
                            label = fromNestedXml(this@Card, parser, XMLNS_TRACT, XML_LABEL)
                            continue@parsingChildren
                        }
                    }
                }

                // try parsing this child element as a content node
                val content = Content.fromXml(this@Card, parser)
                if (content != null) {
                    if (!content.isIgnored) add(content)
                    continue@parsingChildren
                }

                // skip unrecognized nodes
                XmlPullParserUtils.skipTag(parser)
            }
        }
        this.analyticsEvents = analyticsEvents.orEmpty()
        this.label = label
    }
}

// TODO: implement card-background-color on Page & Manifest
val Card?.backgroundColor get() = this?.backgroundColor ?: Manifest.getBackgroundColor(null)
val Card?.backgroundImageGravity get() = this?.backgroundImageGravity ?: DEFAULT_BACKGROUND_IMAGE_GRAVITY
val Card?.backgroundImageScaleType get() = this?.backgroundImageScaleType ?: DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE

// TODO: this should be an instance val
val Card?.backgroundImageResource get() = this?.getResource(backgroundImage)
