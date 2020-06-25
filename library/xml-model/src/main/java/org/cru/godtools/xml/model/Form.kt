package org.cru.godtools.xml.model

import org.cru.godtools.xml.XMLNS_CONTENT
import org.xmlpull.v1.XmlPullParser

class Form internal constructor(parent: BaseModel, parser: XmlPullParser) : Content(parent, parser), Parent {
    companion object {
        internal const val XML_FORM = "form"
    }

    override val content: List<Content>

    init {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_FORM)
        content = parseContent(parser)
    }
}
