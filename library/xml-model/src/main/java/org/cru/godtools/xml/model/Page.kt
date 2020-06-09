package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.RestrictTo
import androidx.annotation.WorkerThread
import org.ccci.gto.android.common.util.XmlPullParserUtils
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.XMLNS_MANIFEST
import org.cru.godtools.xml.XMLNS_TRACT
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

@ColorInt
private const val DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT
private val DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ImageScaleType.FILL_X
private val DEFAULT_BACKGROUND_IMAGE_GRAVITY = ImageGravity.CENTER

private const val XML_MANIFEST_FILENAME = "filename"
private const val XML_MANIFEST_SRC = "src"
private const val XML_CARD_TEXT_COLOR = "card-text-color"
private const val XML_CARDS = "cards"
private const val XML_MODALS = "modals"

class Page : Base, Styles, Parent {
    val id get() = fileName ?: "${manifest.code}-$position"
    val position: Int

    private val fileName: String?
    val localFileName: String?
    var listeners: Set<Event.Id> = emptySet()
        private set

    @ColorInt
    var backgroundColor: Int = DEFAULT_BACKGROUND_COLOR
    var backgroundImage: String? = null
    var backgroundImageGravity: ImageGravity = DEFAULT_BACKGROUND_IMAGE_GRAVITY
    var backgroundImageScaleType: ImageScaleType = DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE
    var header: Header? = null
        private set
    var hero: Hero? = null
        private set
    var cards: List<Card> = emptyList()
        private set
    var modals: List<Modal> = emptyList()
        private set
    var callToAction: CallToAction = CallToAction(this)
        private set

    override fun getPage() = this

    @ColorInt
    private var _primaryColor: Int? = null
    @get:ColorInt
    override val primaryColor get() = _primaryColor ?: stylesParent.primaryColor

    @ColorInt
    private var _primaryTextColor: Int? = null
    @get:ColorInt
    override val primaryTextColor get() = _primaryTextColor ?: stylesParent.primaryTextColor

    @ColorInt
    private var _textColor: Int? = null
    @get:ColorInt
    override val textColor get() = _textColor ?: stylesParent.textColor

    @ColorInt
    private var _cardTextColor: Int? = null
    @get:ColorInt
    val cardTextColor get() = _cardTextColor ?: textColor

    override val content get() = emptyList<Content>()

    @RestrictTo(RestrictTo.Scope.TESTS)
    internal constructor(
        manifest: Manifest,
        position: Int,
        fileName: String? = null,
        localFileName: String? = null
    ) : super(manifest) {
        this.position = position
        this.fileName = fileName
        this.localFileName = localFileName
    }

    internal constructor(manifest: Manifest, position: Int, manifestParser: XmlPullParser) : super(manifest) {
        this.position = position

        manifestParser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_PAGE)
        fileName = manifestParser.getAttributeValue(null, XML_MANIFEST_FILENAME)
        localFileName = manifestParser.getAttributeValue(null, XML_MANIFEST_SRC)
        XmlPullParserUtils.skipTag(manifestParser)
    }

    fun findModal(id: String?) = modals.firstOrNull { it.id.equals(id, ignoreCase = true) }

    private var pageXmlParsed = false

    @WorkerThread
    @Throws(IOException::class, XmlPullParserException::class)
    fun parsePageXml(parser: XmlPullParser) {
        // make sure we haven't parsed this page XML already
        check(!pageXmlParsed) { "Page XML already parsed" }
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_PAGE)

        listeners = parseEvents(parser, XML_LISTENERS)
        _primaryColor = parser.getAttributeValueAsColorOrNull(XML_PRIMARY_COLOR) ?: _primaryColor
        _primaryTextColor = parser.getAttributeValueAsColorOrNull(XML_PRIMARY_TEXT_COLOR) ?: _primaryTextColor
        _textColor = parser.getAttributeValueAsColorOrNull(XML_TEXT_COLOR) ?: _textColor
        _cardTextColor = parser.getAttributeValueAsColorOrNull(XML_CARD_TEXT_COLOR) ?: _cardTextColor
        backgroundColor = parser.getAttributeValueAsColorOrNull(XML_BACKGROUND_COLOR) ?: backgroundColor
        backgroundImage = parser.getAttributeValue(null, XML_BACKGROUND_IMAGE)
        backgroundImageGravity =
            parser.getAttributeValueAsImageGravity(XML_BACKGROUND_IMAGE_GRAVITY, backgroundImageGravity)
        backgroundImageScaleType =
            parser.getAttributeValueAsImageScaleTypeOrNull(XML_BACKGROUND_IMAGE_SCALE_TYPE) ?: backgroundImageScaleType

        // process any child elements
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                XMLNS_TRACT -> when (parser.name) {
                    Header.XML_HEADER -> header = Header(this, parser)
                    Hero.XML_HERO -> hero = Hero(this, parser)
                    XML_CARDS -> cards = parseCardsXml(parser)
                    XML_MODALS -> modals = parseModalsXml(parser)
                    CallToAction.XML_CALL_TO_ACTION -> callToAction = CallToAction(this, parser)
                    else -> XmlPullParserUtils.skipTag(parser)
                }
                else -> XmlPullParserUtils.skipTag(parser)
            }
        }

        // mark page XML as parsed
        pageXmlParsed = true
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun parseCardsXml(parser: XmlPullParser) = buildList {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_CARDS)

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                XMLNS_TRACT -> when (parser.name) {
                    Card.XML_CARD -> add(Card.fromXml(this@Page, parser, size))
                    else -> XmlPullParserUtils.skipTag(parser)
                }
                else -> XmlPullParserUtils.skipTag(parser)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun parseModalsXml(parser: XmlPullParser) = buildList {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_MODALS)

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                XMLNS_TRACT -> when (parser.name) {
                    Modal.XML_MODAL -> add(Modal(this@Page, size, parser))
                    else -> XmlPullParserUtils.skipTag(parser)
                }
                else -> XmlPullParserUtils.skipTag(parser)
            }
        }
    }

    val isLastPage get() = position == manifest.pages.size - 1

    companion object {
        const val XML_PAGE = "page"

        @JvmStatic
        @WorkerThread
        @Throws(XmlPullParserException::class, IOException::class)
        fun fromManifestXml(manifest: Manifest, position: Int, parser: XmlPullParser) = Page(manifest, position, parser)
    }
}

@get:ColorInt
val Page?.backgroundColor get() = this?.backgroundColor ?: DEFAULT_BACKGROUND_COLOR
val Page?.backgroundImageResource get() = this?.getResource(backgroundImage)
val Page?.backgroundImageScaleType get() = this?.backgroundImageScaleType ?: DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE
val Page?.backgroundImageGravity: Int get() = (this?.backgroundImageGravity ?: DEFAULT_BACKGROUND_IMAGE_GRAVITY).gravity
