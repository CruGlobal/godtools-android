package org.cru.godtools.xml.model.tips

import org.cru.godtools.xml.XMLNS_TRAINING
import org.cru.godtools.xml.model.Base
import org.cru.godtools.xml.model.BaseModel
import org.cru.godtools.xml.model.Content
import org.cru.godtools.xml.model.Parent
import org.cru.godtools.xml.model.parseContent
import org.xmlpull.v1.XmlPullParser

class TipPage : BaseModel, Parent {
    companion object {
        internal const val XML_PAGE = "page"
    }

    override val content: List<Content>

    internal constructor(parent: Base, parser: XmlPullParser) : super(parent) {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRAINING, XML_PAGE)
        content = parseContent(parser)
    }
}
