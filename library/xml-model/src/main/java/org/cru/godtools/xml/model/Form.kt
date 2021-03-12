package org.cru.godtools.xml.model

import org.xmlpull.v1.XmlPullParser

class Form internal constructor(parent: Base, parser: XmlPullParser) : Content(parent, parser), Parent {
    companion object {
        internal const val XML_FORM = "form"
    }

    override val content: List<Content>
    override val tips get() = contentTips

    init {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_FORM)
        content = parseContent(parser)
    }
}
