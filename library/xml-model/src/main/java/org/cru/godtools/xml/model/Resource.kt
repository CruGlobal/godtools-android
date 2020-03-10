package org.cru.godtools.xml.model

import androidx.annotation.WorkerThread
import org.ccci.gto.android.common.util.XmlPullParserUtils
import org.cru.godtools.xml.XMLNS_MANIFEST
import org.xmlpull.v1.XmlPullParser

private const val XML_FILENAME = "filename"
private const val XML_SRC = "src"

class Resource : Base {
    internal constructor(manifest: Manifest, parser: XmlPullParser) : super(manifest) {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_RESOURCE)

        name = parser.getAttributeValue(null, XML_FILENAME)
        localName = parser.getAttributeValue(null, XML_SRC)

        // discard any nested nodes
        XmlPullParserUtils.skipTag(parser)
    }

    val name: String?
    val localName: String?

    // TODO: make internal
    companion object {
        const val XML_RESOURCE = "resource"

        @JvmStatic
        @WorkerThread
        @Deprecated(
            "Use Resource constructor instead",
            ReplaceWith("Resource(manifest, parser)", "org.cru.godtools.xml.model.Resource")
        )
        fun fromXml(manifest: Manifest, parser: XmlPullParser) = Resource(manifest, parser)
    }
}
