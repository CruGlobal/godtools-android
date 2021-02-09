package org.cru.godtools.xml.model

import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.RestrictTo
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.XMLNS_CONTENT
import org.xmlpull.v1.XmlPullParser

private const val XML_RESOURCE = "resource"
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

    private val imageStartName: String?
    val imageStart get() = getResource(imageStartName)
    private val imageEndName: String?
    val imageEnd get() = getResource(imageEndName)


    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(
        parent: Base,
        text: String? = null,
        textScale: Double? = null,
        @ColorInt textColor: Int? = null,
        textAlign: Align? = null,
        imageStart: String? = null,
        imageEnd: String? = null
    ) : super(parent) {
        this.text = text
        _textAlign = textAlign
        _textColor = textColor
        _textScale = textScale
        this.imageStartName = imageStart
        this.imageEndName = imageEnd
    }

    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_TEXT)

        _textAlign = Align.parseOrNull(parser.getAttributeValue(null, XML_TEXT_ALIGN))
        _textColor = parser.getAttributeValueAsColorOrNull(XML_TEXT_COLOR)
        _textScale = parser.getAttributeValue(null, XML_TEXT_SCALE)?.toDoubleOrNull()

        imageStartName = parser.getAttributeValue(null, XML_RESOURCE)
        imageEndName = parser.getAttributeValue(null, XML_RESOURCE)

        text = parser.nextText()
    }

    @ColorInt
    fun getTextColor(@ColorInt defColor: Int) = _textColor ?: defColor

    companion object {
        internal const val XML_TEXT = "text"

        internal fun fromNestedXml(
            parent: Base,
            parser: XmlPullParser,
            parentNamespace: String?,
            parentName: String
        ): Text? {
            parser.require(XmlPullParser.START_TAG, parentNamespace, parentName)

            // process any child elements
            var text: Text? = null
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue

                val ns = parser.namespace
                val name = parser.name
                when {
                    ns == XMLNS_CONTENT && name == XML_TEXT -> text = Text(parent, parser)
                    else -> parser.skipTag()
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
