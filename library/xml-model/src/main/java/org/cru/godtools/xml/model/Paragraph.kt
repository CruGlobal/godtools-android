package org.cru.godtools.xml.model

import org.ccci.gto.android.common.util.XmlPullParserUtils
import org.cru.godtools.xml.XMLNS_CONTENT
import org.xmlpull.v1.XmlPullParser
import java.util.Collections

class Paragraph : Content, Parent {
    companion object {
        internal const val XML_PARAGRAPH = "paragraph"
    }

    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_PARAGRAPH)

        // process any child elements
        val contentList = mutableListOf<Content>()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            // try parsing this child element as a content node
            val content = Content.fromXml(this, parser)
            if (content != null) {
                if (!content.isIgnored) contentList.add(content)
                continue
            }

            // skip any unprocessed tags
            XmlPullParserUtils.skipTag(parser)
        }
        content = Collections.unmodifiableList(contentList)
    }

    override val content: List<Content>
}
