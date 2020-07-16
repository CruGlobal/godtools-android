package org.cru.godtools.xml.model

import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.XMLNS_MANIFEST
import org.xmlpull.v1.XmlPullParser

private const val XML_FILENAME = "filename"
private const val XML_SRC = "src"

class Resource : BaseModel {
    internal companion object {
        internal const val XML_RESOURCE = "resource"
    }

    internal constructor(manifest: Manifest, parser: XmlPullParser) : super(manifest) {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_RESOURCE)

        name = parser.getAttributeValue(null, XML_FILENAME)
        localName = parser.getAttributeValue(null, XML_SRC)

        parser.skipTag()
    }

    val name: String?
    val localName: String?
}
