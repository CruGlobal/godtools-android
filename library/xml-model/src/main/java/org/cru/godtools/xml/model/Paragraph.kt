package org.cru.godtools.xml.model

import androidx.annotation.RestrictTo
import org.cru.godtools.xml.XMLNS_CONTENT
import org.xmlpull.v1.XmlPullParser

class Paragraph : Content, Parent {
    companion object {
        internal const val XML_PARAGRAPH = "paragraph"
        internal const val XML_FALLBACK = "fallback"
    }

    override val content: List<Content>
    override val tips get() = contentTips

    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_PARAGRAPH)
        content = parseContent(parser)
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(parent: Base, content: ((Paragraph) -> List<Content>?)? = null) : super(parent) {
        this.content = content?.invoke(this).orEmpty()
    }
}
