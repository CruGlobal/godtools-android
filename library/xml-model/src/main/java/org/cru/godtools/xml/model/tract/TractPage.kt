package org.cru.godtools.xml.model.tract

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.RestrictTo
import androidx.annotation.WorkerThread
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.XMLNS_TRACT
import org.cru.godtools.xml.model.BaseModel
import org.cru.godtools.xml.model.ImageGravity
import org.cru.godtools.xml.model.ImageScaleType
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Styles
import org.cru.godtools.xml.model.getAttributeValueAsColorOrNull
import org.cru.godtools.xml.model.getAttributeValueAsImageGravity
import org.cru.godtools.xml.model.getAttributeValueAsImageScaleTypeOrNull
import org.cru.godtools.xml.model.primaryColor
import org.cru.godtools.xml.model.primaryTextColor
import org.cru.godtools.xml.model.textColor
import org.xmlpull.v1.XmlPullParser

@ColorInt
private const val DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT
private val DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ImageScaleType.FILL_X
private val DEFAULT_BACKGROUND_IMAGE_GRAVITY = ImageGravity.CENTER

private const val XML_PAGE = "page"
private const val XML_CARD_TEXT_COLOR = "card-text-color"
private const val XML_CARDS = "cards"
private const val XML_MODALS = "modals"

class TractPage : BaseModel, Styles {
    val id get() = fileName ?: "${manifest.code}-$position"
    val position: Int

    private val fileName: String?
    val listeners: Set<Event.Id>

    @ColorInt
    val backgroundColor: Int
    private val _backgroundImage: String?
    val backgroundImage get() = getResource(_backgroundImage)
    val backgroundImageGravity: ImageGravity
    val backgroundImageScaleType: ImageScaleType

    val header: Header?
    val hero: Hero?
    val cards: List<Card>
    val visibleCards get() = cards.filter { !it.isHidden }
    val modals: List<Modal>
    val callToAction: CallToAction

    @ColorInt
    private val _primaryColor: Int?
    @get:ColorInt
    override val primaryColor get() = _primaryColor ?: stylesParent.primaryColor

    @ColorInt
    private val _primaryTextColor: Int?
    @get:ColorInt
    override val primaryTextColor get() = _primaryTextColor ?: stylesParent.primaryTextColor

    @ColorInt
    private val _textColor: Int?
    @get:ColorInt
    override val textColor get() = _textColor ?: stylesParent.textColor

    @ColorInt
    private val _cardTextColor: Int?
    @get:ColorInt
    val cardTextColor get() = _cardTextColor ?: textColor

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(
        manifest: Manifest,
        position: Int = 0,
        fileName: String? = null,
        cards: ((TractPage) -> List<Card>?)? = null,
        callToAction: ((TractPage) -> CallToAction?)? = null
    ) : super(manifest) {
        this.position = position
        this.fileName = fileName

        listeners = emptySet()

        _primaryColor = null
        _primaryTextColor = null
        _textColor = null
        _cardTextColor = null

        backgroundColor = DEFAULT_BACKGROUND_COLOR
        _backgroundImage = null
        backgroundImageGravity = DEFAULT_BACKGROUND_IMAGE_GRAVITY
        backgroundImageScaleType = DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE

        header = null
        hero = null
        this.cards = cards?.invoke(this).orEmpty()
        modals = emptyList()
        this.callToAction = callToAction?.invoke(this) ?: CallToAction(this)
    }

    @WorkerThread
    internal constructor(
        manifest: Manifest,
        position: Int,
        fileName: String?,
        parser: XmlPullParser?
    ) : super(manifest) {
        this.position = position
        this.fileName = fileName

        parser?.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_PAGE)

        listeners = parser?.let { parseEvents(it, XML_LISTENERS) }.orEmpty()
        _primaryColor = parser?.getAttributeValueAsColorOrNull(XML_PRIMARY_COLOR)
        _primaryTextColor = parser?.getAttributeValueAsColorOrNull(XML_PRIMARY_TEXT_COLOR)
        _textColor = parser?.getAttributeValueAsColorOrNull(XML_TEXT_COLOR)
        _cardTextColor = parser?.getAttributeValueAsColorOrNull(XML_CARD_TEXT_COLOR)

        backgroundColor = parser?.getAttributeValueAsColorOrNull(XML_BACKGROUND_COLOR) ?: DEFAULT_BACKGROUND_COLOR
        _backgroundImage = parser?.getAttributeValue(null, XML_BACKGROUND_IMAGE)
        backgroundImageGravity =
            parser.getAttributeValueAsImageGravity(XML_BACKGROUND_IMAGE_GRAVITY, DEFAULT_BACKGROUND_IMAGE_GRAVITY)
        backgroundImageScaleType = parser?.getAttributeValueAsImageScaleTypeOrNull(XML_BACKGROUND_IMAGE_SCALE_TYPE)
            ?: DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE

        var header: Header? = null
        var hero: Hero? = null
        var cards: List<Card>? = null
        var modals: List<Modal>? = null
        var callToAction: CallToAction? = null
        if (parser != null) {
            // process any child elements
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue

                when (parser.namespace) {
                    XMLNS_TRACT -> when (parser.name) {
                        Header.XML_HEADER -> header = Header(this, parser)
                        Hero.XML_HERO -> hero = Hero(this, parser)
                        XML_CARDS -> cards = parser.parseCardsXml()
                        XML_MODALS -> modals = parser.parseModalsXml()
                        CallToAction.XML_CALL_TO_ACTION -> callToAction = CallToAction(this, parser)
                        else -> parser.skipTag()
                    }
                    else -> parser.skipTag()
                }
            }
        }
        this.header = header
        this.hero = hero
        this.cards = cards.orEmpty()
        this.modals = modals.orEmpty()
        this.callToAction = callToAction ?: CallToAction(this)
    }

    fun findModal(id: String?) = modals.firstOrNull { it.id.equals(id, ignoreCase = true) }

    @OptIn(ExperimentalStdlibApi::class)
    private fun XmlPullParser.parseCardsXml() = buildList {
        require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_CARDS)

        while (next() != XmlPullParser.END_TAG) {
            if (eventType != XmlPullParser.START_TAG) continue

            when (namespace) {
                XMLNS_TRACT -> when (name) {
                    Card.XML_CARD -> add(Card(this@TractPage, size, this@parseCardsXml))
                    else -> skipTag()
                }
                else -> skipTag()
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun XmlPullParser.parseModalsXml() = buildList {
        require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_MODALS)

        while (next() != XmlPullParser.END_TAG) {
            if (eventType != XmlPullParser.START_TAG) continue

            when (namespace) {
                XMLNS_TRACT -> when (name) {
                    Modal.XML_MODAL -> add(Modal(this@TractPage, size, this@parseModalsXml))
                    else -> skipTag()
                }
                else -> skipTag()
            }
        }
    }

    val isLastPage get() = position == manifest.tractPages.size - 1
}

@get:ColorInt
val TractPage?.backgroundColor get() = this?.backgroundColor ?: DEFAULT_BACKGROUND_COLOR
val TractPage?.backgroundImageScaleType get() = this?.backgroundImageScaleType ?: DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE
val TractPage?.backgroundImageGravity get() = (this?.backgroundImageGravity ?: DEFAULT_BACKGROUND_IMAGE_GRAVITY).gravity
