package org.cru.godtools.xml.model

import androidx.annotation.RestrictTo
import androidx.annotation.WorkerThread
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.xmlpull.v1.XmlPullParser

private const val XML_HEIGHT = "height"
private const val XML_MODE = "mode"
private const val XML_MODE_AUTO = "auto"
private const val XML_MODE_FIXED = "fixed"

class Spacer : Content {
    companion object {
        internal const val XML_SPACER = "spacer"
    }

    enum class Mode {
        AUTO, FIXED;

        companion object {
            val DEFAULT = AUTO

            internal fun parseOrNull(value: String?) = when (value) {
                XML_MODE_AUTO -> AUTO
                XML_MODE_FIXED -> FIXED
                else -> null
            }
        }
    }

    val mode: Mode
    val height: Int

    @WorkerThread
    constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_SPACER)

        mode = Mode.parseOrNull(parser.getAttributeValue(null, XML_MODE)) ?: Mode.DEFAULT
        height = parser.getAttributeValue(null, XML_HEIGHT)?.toIntOrNull()?.coerceAtLeast(0) ?: 0

        parser.skipTag()
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(parent: Base, mode: Mode = Mode.AUTO, height: Int = 0) : super(parent) {
        this.mode = mode
        this.height = height
    }
}
