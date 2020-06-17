package org.cru.godtools.xml.model

import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.RestrictTo
import org.ccci.gto.android.common.util.XmlPullParserUtils
import org.cru.godtools.xml.XMLNS_CONTENT
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

private const val XML_TEXT_ALIGN = "text-align"
private const val XML_TEXT_ALIGN_START = "start"
private const val XML_TEXT_ALIGN_CENTER = "center"
private const val XML_TEXT_ALIGN_END = "end"
private const val XML_TEXT_SCALE = "text-scale"

private const val DEFAULT_TEXT_SCALE = 1.0

class Text : Content {
    enum class Align(val gravity: Int) {
        START(Gravity.START), CENTER(Gravity.CENTER_HORIZONTAL), END(Gravity.END);

        companion object {
            @JvmField
            val DEFAULT = START

            fun parseOrNull(value: String?) = when (value) {
                XML_TEXT_ALIGN_START -> START
                XML_TEXT_ALIGN_CENTER -> CENTER
                XML_TEXT_ALIGN_END -> END
                else -> null
            }
        }
    }

    val text: String?

    private val _textAlign: Align?
    val textAlign get() = _textAlign ?: stylesParent.textAlign
    @ColorInt
    private val _textColor: Int?
    @get:ColorInt
    val textColor get() = _textColor ?: defaultTextColor
    private val _textScale: Double?
    val textScale get() = _textScale ?: DEFAULT_TEXT_SCALE

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(parent: Base, text: String?, textScale: Double?, @ColorInt textColor: Int?, textAlign: Align?) :
        super(parent) {
        this.text = text
        _textAlign = textAlign
        _textColor = textColor
        _textScale = textScale
    }

    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_TEXT)

        _textAlign = Align.parseOrNull(parser.getAttributeValue(null, XML_TEXT_ALIGN))
        _textColor = parser.getAttributeValueAsColorOrNull(XML_TEXT_COLOR)
        _textScale = parser.getAttributeValue(null, XML_TEXT_SCALE)?.toDoubleOrNull()

        text = parser.nextText()
    }

    @ColorInt
    fun getTextColor(@ColorInt defColor: Int) = _textColor ?: defColor

    companion object {
        // TODO: make internal
        const val XML_TEXT = "text"

        @JvmStatic
        @Throws(IOException::class, XmlPullParserException::class)
        fun fromNestedXml(parent: Base, parser: XmlPullParser, parentNamespace: String?, parentName: String): Text? {
            parser.require(XmlPullParser.START_TAG, parentNamespace, parentName)

            // process any child elements
            var text: Text? = null
            parsingChildren@ while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue

                when (parser.namespace to parser.name) {
                    XMLNS_CONTENT to XML_TEXT -> text = Text(parent, parser)
                    else -> XmlPullParserUtils.skipTag(parser)
                }
            }
            return text
        }
    }
}

@get:ColorInt
val Text?.defaultTextColor get() = stylesParent.textColor

@Deprecated("Use null-safe accessor directly", ReplaceWith("this?.text"))
val Text?.text get() = this?.text

val Text?.textAlign get() = this?.textAlign ?: Text.Align.DEFAULT
@get:ColorInt
val Text?.textColor get() = this?.textColor ?: stylesParent.textColor
val Text?.textScale get() = this?.textScale ?: DEFAULT_TEXT_SCALE
@get:DimenRes
val Text?.textSize get() = stylesParent.textSize
