package org.cru.godtools.xml.model.tips

import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.XMLNS_TRAINING
import org.cru.godtools.xml.model.Base
import org.cru.godtools.xml.model.Content
import org.xmlpull.v1.XmlPullParser

private const val XML_ID = "id"

class InlineTip : Content {
    companion object {
        internal const val XML_TIP = "tip"
    }

    val id: String?

    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRAINING, XML_TIP)

        id = parser.getAttributeValue(null, XML_ID)

        parser.skipTag()
    }
}
