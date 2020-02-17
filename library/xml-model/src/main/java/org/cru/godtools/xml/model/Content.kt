package org.cru.godtools.xml.model

import androidx.annotation.CallSuper
import org.cru.godtools.xml.Constants
import org.xmlpull.v1.XmlPullParser

private const val XML_RESTRICT_TO = "restrictTo"

abstract class Content protected constructor(parent: Base) : Base(parent) {
    protected constructor(parent: Base, parser: XmlPullParser) : this(parent) {
        parseAttrs(parser)
    }

    private lateinit var restrictTo: Set<DeviceType>

    /**
     * @return true if this content element should be completely ignored.
     */
    val isIgnored get() = !restrictTo.contains(DeviceType.MOBILE)

    @CallSuper
    protected open fun parseAttrs(parser: XmlPullParser) {
        restrictTo = DeviceType.parse(
            types = parser.getAttributeValue(null, XML_RESTRICT_TO),
            defValue = DeviceType.ALL
        )
    }

    companion object {
        fun fromXml(parent: Base, parser: XmlPullParser): Content? {
            parser.require(XmlPullParser.START_TAG, null, null)
            return when (parser.namespace) {
                Constants.XMLNS_CONTENT -> when (parser.name) {
                    Paragraph.XML_PARAGRAPH -> Paragraph(parent, parser)
                    Tabs.XML_TABS -> Tabs.fromXml(parent, parser)
                    Text.XML_TEXT -> Text.fromXml(parent, parser)
                    Image.XML_IMAGE -> Image(parent, parser)
                    Button.XML_BUTTON -> Button.fromXml(parent, parser)
                    Form.XML_FORM -> Form(parent, parser)
                    Input.XML_INPUT -> Input.fromXml(parent, parser)
                    Link.XML_LINK -> Link.fromXml(parent, parser)
                    else -> null
                }
                else -> null
            }
        }
    }
}
