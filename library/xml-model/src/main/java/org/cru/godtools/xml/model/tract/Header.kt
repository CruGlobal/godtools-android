package org.cru.godtools.xml.model.tract

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.VisibleForTesting
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.model.BaseModel
import org.cru.godtools.xml.model.Styles
import org.cru.godtools.xml.model.Text
import org.cru.godtools.xml.model.XMLNS_TRACT
import org.cru.godtools.xml.model.XMLNS_TRAINING
import org.cru.godtools.xml.model.getAttributeValueAsColorOrNull
import org.cru.godtools.xml.model.parseTextChild
import org.xmlpull.v1.XmlPullParser

private const val XML_NUMBER = "number"
private const val XML_TITLE = "title"
private const val XML_TIP = "tip"

class Header internal constructor(private val page: TractPage, parser: XmlPullParser) : BaseModel(page), Styles {
    companion object {
        internal const val XML_HEADER = "header"
    }

    @ColorInt
    private val _backgroundColor: Int?
    @get:ColorInt
    internal val backgroundColor get() = _backgroundColor ?: page.primaryColor

    @get:ColorInt
    override val textColor get() = primaryTextColor

    val number: Text?
    val title: Text?

    @VisibleForTesting
    internal val tipId: String?
    val tip get() = manifest.findTip(tipId)

    init {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_HEADER)
        _backgroundColor = parser.getAttributeValueAsColorOrNull(XML_BACKGROUND_COLOR)
        tipId = parser.getAttributeValue(XMLNS_TRAINING, XML_TIP)

        // process any child elements
        var number: Text? = null
        var title: Text? = null
        parsingChildren@ while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                XMLNS_TRACT -> when (parser.name) {
                    XML_NUMBER -> number = parser.parseTextChild(this, XMLNS_TRACT, XML_NUMBER)
                    XML_TITLE -> title = parser.parseTextChild(this, XMLNS_TRACT, XML_TITLE)
                    else -> parser.skipTag()
                }
                else -> parser.skipTag()
            }
        }
        this.number = number
        this.title = title
    }
}

val Header?.backgroundColor get() = this?.backgroundColor ?: Color.TRANSPARENT
