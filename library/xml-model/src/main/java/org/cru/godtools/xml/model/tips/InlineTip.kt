package org.cru.godtools.xml.model.tips

import androidx.annotation.RestrictTo
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.model.Base
import org.cru.godtools.xml.model.Content
import org.cru.godtools.xml.model.XMLNS_TRAINING
import org.xmlpull.v1.XmlPullParser

private const val XML_ID = "id"

class InlineTip : Content {
    companion object {
        internal const val XML_TIP = "tip"
    }

    val id: String?
    val tip: Tip? get() = manifest.findTip(id)

    override val tips get() = listOfNotNull(tip)

    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRAINING, XML_TIP)

        id = parser.getAttributeValue(null, XML_ID)

        parser.skipTag()
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    constructor(parent: Base, id: String? = null) : super(parent) {
        this.id = id
    }
}
