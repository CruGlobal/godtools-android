package org.cru.godtools.xml.model

import androidx.annotation.RestrictTo
import org.cru.godtools.xml.XMLNS_CONTENT
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

class Fallback : Content, Parent {
    companion object {
        internal const val XML_FALLBACK = "fallback"
    }

    override val content: List<Content>

    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        when (parser.name) {
            Paragraph.XML_PARAGRAPH -> {
                parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, Paragraph.XML_PARAGRAPH)
                if (parser.getAttributeValue(null, Paragraph.XML_FALLBACK)?.toBoolean() != true) {
                    throw XmlPullParserException("expected fallback=\"true\" at ${parser.positionDescription}")
                }
            }
            else -> parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_FALLBACK)
        }

        content = parseContent(parser)
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(parent: Base, content: ((Fallback) -> List<Content>?)? = null) : super(parent) {
        this.content = content?.invoke(this).orEmpty()
    }
}
