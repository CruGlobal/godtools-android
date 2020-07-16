package org.cru.godtools.xml.model

import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.R
import org.cru.godtools.xml.XMLNS_CONTENT
import org.xmlpull.v1.XmlPullParser

private const val XML_TYPE = "type"
private const val XML_TYPE_TEXT = "text"
private const val XML_TYPE_EMAIL = "email"
private const val XML_TYPE_PHONE = "phone"
private const val XML_TYPE_HIDDEN = "hidden"
private const val XML_NAME = "name"
private const val XML_REQUIRED = "required"
private const val XML_VALUE = "value"
private const val XML_LABEL = "label"
private const val XML_PLACEHOLDER = "placeholder"
private val VALIDATE_EMAIL = Regex(".+@.+")

class Input : Content {
    companion object {
        internal const val XML_INPUT = "input"
    }

    enum class Type {
        TEXT, EMAIL, PHONE, HIDDEN;

        companion object {
            internal val DEFAULT = TEXT

            internal fun parseOrNull(type: String?) = when (type) {
                XML_TYPE_EMAIL -> EMAIL
                XML_TYPE_HIDDEN -> HIDDEN
                XML_TYPE_PHONE -> PHONE
                XML_TYPE_TEXT -> TEXT
                else -> null
            }
        }
    }

    class Error internal constructor(
        @StringRes val msgId: Int?,
        val msg: String? = null
    )

    val type: Type
    val name: String?
    val value: String?
    @VisibleForTesting
    internal val required: Boolean
    val label: Text?
    val placeholder: Text?

    @WorkerThread
    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_INPUT)

        type = Type.parseOrNull(parser.getAttributeValue(null, XML_TYPE)) ?: Type.DEFAULT
        name = parser.getAttributeValue(null, XML_NAME)
        value = parser.getAttributeValue(null, XML_VALUE)
        required = parser.getAttributeValue(null, XML_REQUIRED)?.toBoolean() ?: false

        // process any child elements
        var label: Text? = null
        var placeholder: Text? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                XMLNS_CONTENT -> when (parser.name) {
                    XML_LABEL -> label = Text.fromNestedXml(this, parser, XMLNS_CONTENT, XML_LABEL)
                    XML_PLACEHOLDER -> placeholder = Text.fromNestedXml(this, parser, XMLNS_CONTENT, XML_PLACEHOLDER)
                    else -> parser.skipTag()
                }
                else -> parser.skipTag()
            }
        }
        this.label = label
        this.placeholder = placeholder
    }

    fun validateValue(raw: String?) = when {
        required && raw.isNullOrBlank() -> Error(R.string.tract_content_input_error_required)
        type == Type.EMAIL && !raw.isNullOrBlank() && !VALIDATE_EMAIL.matches(raw) ->
            Error(R.string.tract_content_input_error_invalid_email)
        else -> null
    }
}

val Input?.type get() = this?.type ?: Input.Type.DEFAULT
