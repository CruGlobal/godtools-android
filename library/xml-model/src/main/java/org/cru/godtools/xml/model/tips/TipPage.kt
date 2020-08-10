package org.cru.godtools.xml.model.tips

import org.cru.godtools.xml.XMLNS_TRAINING
import org.cru.godtools.xml.model.BaseModel
import org.cru.godtools.xml.model.Content
import org.cru.godtools.xml.model.Parent
import org.cru.godtools.xml.model.parseContent
import org.xmlpull.v1.XmlPullParser

class TipPage : BaseModel, Parent {
    companion object {
        internal const val XML_PAGE = "page"
    }

    private val tip: Tip
    val position: Int

    override val content: List<Content>

    internal constructor(tip: Tip, position: Int, parser: XmlPullParser) : super(tip) {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRAINING, XML_PAGE)
        this.tip = tip
        this.position = position
        content = parseContent(parser)
    }

    val isLastPage get() = position == tip.pages.size - 1
}
