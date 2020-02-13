package org.cru.godtools.xml.model

import org.ccci.gto.android.common.util.XmlPullParserUtils
import org.cru.godtools.xml.Constants
import org.xmlpull.v1.XmlPullParser
import java.util.Collections

class Paragraph : Content, Parent {
    internal constructor(parent: Base, parser: XmlPullParser) : super(parent) {
        parser.require(XmlPullParser.START_TAG, Constants.XMLNS_CONTENT, XML_PARAGRAPH)
        parseAttrs(parser)

        // process any child elements
        val contentList = mutableListOf<Content>()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            // try parsing this child element as a content node
            val content = Content.fromXml(this, parser)
            when {
                content == null -> XmlPullParserUtils.skipTag(parser)
                !content.isIgnored -> contentList.add(content)
            }
        }
        content = Collections.unmodifiableList(contentList)
    }

    override val content: List<Content>

    // TODO: make this internal
    companion object {
        const val XML_PARAGRAPH = "paragraph"

        @JvmStatic
        @Deprecated(
            "Use Paragraph constructor instead",
            ReplaceWith("Paragraph(parent, parser)", "org.cru.godtools.xml.model.Paragraph")
        )
        fun fromXml(parent: Base, parser: XmlPullParser) = Paragraph(parent, parser)
    }
}
