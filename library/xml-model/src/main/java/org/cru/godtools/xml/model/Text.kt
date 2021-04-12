package org.cru.godtools.xml.model

import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.Dimension
import androidx.annotation.Dimension.DP
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.xmlpull.v1.XmlPullParser

private const val XML_END_IMAGE = "end-image"
private const val XML_END_IMAGE_SIZE = "end-image-size"
private const val XML_START_IMAGE = "start-image"
private const val XML_START_IMAGE_SIZE = "start-image-size"
private const val XML_TEXT_ALIGN = "text-align"
private const val XML_TEXT_ALIGN_START = "start"
private const val XML_TEXT_ALIGN_CENTER = "center"
private const val XML_TEXT_ALIGN_END = "end"
private const val XML_TEXT_STYLE = "text-style"
private const val XML_TEXT_STYLE_BOLD = "bold"
private const val XML_TEXT_STYLE_ITALIC = "italic"
private const val XML_TEXT_STYLE_UNDERLINE = "underline"

class Text : Content {
    companion object {
        internal const val XML_TEXT = "text"

        @VisibleForTesting
        @Dimension(unit = DP)
        internal const val DEFAULT_IMAGE_SIZE = 40
    }

    enum class Align(val gravity: Int) {
        START(Gravity.START), CENTER(Gravity.CENTER_HORIZONTAL), END(Gravity.END);

        companion object {
            val DEFAULT = START

            fun parseOrNull(value: String?) = when (value) {
                XML_TEXT_ALIGN_START -> START
                XML_TEXT_ALIGN_CENTER -> CENTER
                XML_TEXT_ALIGN_END -> END
                else -> null
            }
        }
    }

    enum class Style {
        BOLD, ITALIC, UNDERLINE;

        companion object {
            internal fun parseOrNull(value: String?) = when (value) {
                XML_TEXT_STYLE_BOLD -> BOLD
                XML_TEXT_STYLE_ITALIC -> ITALIC
                XML_TEXT_STYLE_UNDERLINE -> UNDERLINE
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
    private val _textScale: Double
    val textScale get() = _textScale * stylesParent.textScale
    val textStyles: Set<Style>

    @VisibleForTesting
    internal val startImageName: String?
    val startImage get() = getResource(startImageName)
    @Dimension(unit = DP)
    val startImageSize: Int
    @VisibleForTesting
    internal val endImageName: String?
    val endImage get() = getResource(endImageName)
    @Dimension(unit = DP)
    val endImageSize: Int

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(
        parent: Base,
        text: String? = null,
        textScale: Double = DEFAULT_TEXT_SCALE,
        @ColorInt textColor: Int? = null,
        textAlign: Align? = null,
        textStyles: Set<Style> = emptySet(),
        startImage: String? = null,
        endImage: String? = null,
    ) : super(parent) {
        this.text = text
        _textAlign = textAlign
        _textColor = textColor
        _textScale = textScale
        this.textStyles = textStyles
        startImageName = startImage
        startImageSize = DEFAULT_IMAGE_SIZE
        endImageName = endImage
        endImageSize = DEFAULT_IMAGE_SIZE
    }

    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_TEXT)

        _textAlign = Align.parseOrNull(parser.getAttributeValue(null, XML_TEXT_ALIGN))
        _textColor = parser.getAttributeValueAsColorOrNull(XML_TEXT_COLOR)
        _textScale = parser.getAttributeValue(null, XML_TEXT_SCALE)?.toDoubleOrNull() ?: DEFAULT_TEXT_SCALE
        textStyles = parser.getAttributeValueAsTextStylesOrNull(XML_TEXT_STYLE).orEmpty()

        startImageName = parser.getAttributeValue(null, XML_START_IMAGE)
        startImageSize = parser.getAttributeValue(null, XML_START_IMAGE_SIZE)?.toIntOrNull() ?: DEFAULT_IMAGE_SIZE
        endImageName = parser.getAttributeValue(null, XML_END_IMAGE)
        endImageSize = parser.getAttributeValue(null, XML_END_IMAGE_SIZE)?.toIntOrNull() ?: DEFAULT_IMAGE_SIZE

        text = parser.nextText()
    }

    @ColorInt
    fun getTextColor(@ColorInt defColor: Int) = _textColor ?: defColor
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

internal fun XmlPullParser.parseTextChild(
    parent: Base,
    parentNamespace: String?,
    parentName: String,
    block: () -> Unit = { }
): Text? {
    require(XmlPullParser.START_TAG, parentNamespace, parentName)

    // process any child elements
    var text: Text? = null
    while (next() != XmlPullParser.END_TAG) {
        if (eventType != XmlPullParser.START_TAG) continue

        // execute any custom parsing logic from the call-site
        // if the block consumes the tag, the parser will be on an END_TAG after returning
        block()
        if (eventType == XmlPullParser.END_TAG) continue

        // parse text node
        when {
            namespace == XMLNS_CONTENT && name == Text.XML_TEXT -> text = Text(parent, this)
            else -> skipTag()
        }
    }
    return text
}

private fun XmlPullParser.getAttributeValueAsTextStylesOrNull(name: String) =
    getAttributeValue(null, name)?.let { REGEX_SEQUENCE_SEPARATOR.split(it) }
        ?.mapNotNullTo(mutableSetOf()) { Text.Style.parseOrNull(it) }
