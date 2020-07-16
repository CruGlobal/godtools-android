package org.cru.godtools.xml.model

import android.graphics.Color
import androidx.annotation.ColorInt
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.XMLNS_TRACT
import org.xmlpull.v1.XmlPullParser

private const val XML_NUMBER = "number"
private const val XML_TITLE = "title"

class Header internal constructor(parent: Page, parser: XmlPullParser) : BaseModel(parent), Styles {
    @ColorInt
    private val _backgroundColor: Int?
    @get:ColorInt
    internal val backgroundColor get() = _backgroundColor ?: page.primaryColor
    val number: Text?
    val title: Text?

    @get:ColorInt
    override val textColor get() = primaryTextColor

    init {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_HEADER)
        _backgroundColor = parser.getAttributeValueAsColorOrNull(XML_BACKGROUND_COLOR)

        // process any child elements
        var number: Text? = null
        var title: Text? = null
        parsingChildren@ while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                XMLNS_TRACT -> when (parser.name) {
                    XML_NUMBER -> number = Text.fromNestedXml(this, parser, XMLNS_TRACT, XML_NUMBER)
                    XML_TITLE -> title = Text.fromNestedXml(this, parser, XMLNS_TRACT, XML_TITLE)
                    else -> parser.skipTag()
                }
                else -> parser.skipTag()
            }
        }
        this.number = number
        this.title = title
    }

    companion object {
        // TODO: make internal
        const val XML_HEADER = "header"

        @JvmStatic
        @Deprecated(
            "Use constructor directly",
            ReplaceWith("Header(parent, parser)", "org.cru.godtools.xml.model.Header")
        )
        fun fromXml(parent: Page, parser: XmlPullParser) = Header(parent, parser)
    }
}

val Header?.backgroundColor get() = this?.backgroundColor ?: Color.TRANSPARENT
