package org.cru.godtools.xml.model

import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.XMLNS_CONTENT
import org.xmlpull.v1.XmlPullParser

private const val XML_END_IMAGE = "end-image"
private const val XML_END_IMAGE_SIZE = "end-image-size"
private const val XML_START_IMAGE = "start-image"
private const val XML_START_IMAGE_SIZE = "start-image-size"
private const val XML_TEXT_ALIGN = "text-align"
private const val XML_TEXT_ALIGN_START = "start"
private const val XML_TEXT_ALIGN_CENTER = "center"
private const val XML_TEXT_ALIGN_END = "end"
private const val XML_TEXT_SCALE = "text-scale"

class Text : Content {
    companion object {
        internal const val XML_TEXT = "text"

        @VisibleForTesting
        internal const val DEFAULT_TEXT_SCALE = 1.0
        @VisibleForTesting
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

    val text: String?

    private val _textAlign: Align?
    val textAlign get() = _textAlign ?: stylesParent.textAlign
    @ColorInt
    private val _textColor: Int?
    @get:ColorInt
    val textColor get() = _textColor ?: defaultTextColor
    private val _textScale: Double?
    val textScale get() = _textScale ?: DEFAULT_TEXT_SCALE

    @VisibleForTesting
    internal val startImageName: String?
    val startImage get() = getResource(startImageName)
    val startImageSize: Int
    @VisibleForTesting
    internal val endImageName: String?
    val endImage get() = getResource(endImageName)
    val endImageSize: Int

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(
        parent: Base,
        text: String? = null,
        textScale: Double? = null,
        @ColorInt textColor: Int? = null,
        textAlign: Align? = null,
        startImage: String? = null,
        startImageSize: Int = DEFAULT_IMAGE_SIZE,
        endImage: String? = null,
        endImageSize: Int = DEFAULT_IMAGE_SIZE,
    ) : super(parent) {
        this.text = text
        _textAlign = textAlign
        _textColor = textColor
        _textScale = textScale
        startImageName = startImage
        this.startImageSize = startImageSize
        endImageName = endImage
        this.endImageSize = endImageSize
    }

    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_TEXT)

        _textAlign = Align.parseOrNull(parser.getAttributeValue(null, XML_TEXT_ALIGN))
        _textColor = parser.getAttributeValueAsColorOrNull(XML_TEXT_COLOR)
        _textScale = parser.getAttributeValue(null, XML_TEXT_SCALE)?.toDoubleOrNull()

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
val Text?.textScale get() = this?.textScale ?: Text.DEFAULT_TEXT_SCALE
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
