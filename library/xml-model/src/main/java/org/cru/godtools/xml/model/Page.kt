package org.cru.godtools.xml.model

import androidx.annotation.ColorInt
import org.ccci.gto.android.common.util.XmlPullParserUtils
import org.cru.godtools.xml.XMLNS_TRACT
import org.xmlpull.v1.XmlPullParser

@OptIn(ExperimentalStdlibApi::class)
fun Page.parseCardsXml(parser: XmlPullParser): List<Card> {
    parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, Page.XML_CARDS)

    return buildList {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                XMLNS_TRACT -> when (parser.name) {
                    Card.XML_CARD -> add(Card.fromXml(this@parseCardsXml, parser, size))
                    else -> XmlPullParserUtils.skipTag(parser)
                }
                else -> XmlPullParserUtils.skipTag(parser)
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
fun Page.parseModalsXml(parser: XmlPullParser): List<Modal> {
    parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, Page.XML_MODALS)

    return buildList {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                XMLNS_TRACT -> when (parser.name) {
                    Modal.XML_MODAL -> add(Modal(this@parseModalsXml, size, parser))
                    else -> XmlPullParserUtils.skipTag(parser)
                }
                else -> XmlPullParserUtils.skipTag(parser)
            }
        }
    }
}

@get:ColorInt
val Page?.backgroundColor get() = this?.mBackgroundColor ?: Page.DEFAULT_BACKGROUND_COLOR
val Page?.backgroundImageResource get() = this?.getResource(mBackgroundImage)
val Page?.backgroundImageScaleType get() = this?.mBackgroundImageScaleType ?: Page.DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE
val Page?.backgroundImageGravity get() = this?.mBackgroundImageGravity ?: Page.DEFAULT_BACKGROUND_IMAGE_GRAVITY
