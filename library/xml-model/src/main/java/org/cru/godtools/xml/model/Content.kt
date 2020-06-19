package org.cru.godtools.xml.model

import org.cru.godtools.xml.XMLNS_CONTENT
import org.xmlpull.v1.XmlPullParser

private const val XML_RESTRICT_TO = "restrictTo"

abstract class Content : Base {
    private val restrictTo: Set<DeviceType>

    protected constructor(parent: BaseModel) : super(parent) {
        restrictTo = DeviceType.ALL
    }

    protected constructor(parent: BaseModel, parser: XmlPullParser) : super(parent) {
        restrictTo = DeviceType.parse(
            types = parser.getAttributeValue(null, XML_RESTRICT_TO),
            defValue = DeviceType.ALL
        )
    }

    /**
     * @return true if this content element should be completely ignored.
     */
    val isIgnored get() = !restrictTo.contains(DeviceType.MOBILE)

    companion object {
        fun fromXml(parent: Base, parser: XmlPullParser): Content? {
            parser.require(XmlPullParser.START_TAG, null, null)
            return when (parser.namespace) {
                XMLNS_CONTENT -> when (parser.name) {
                    Paragraph.XML_PARAGRAPH -> Paragraph(parent, parser)
                    Tabs.XML_TABS -> Tabs(parent, parser)
                    Text.XML_TEXT -> Text(parent, parser)
                    Image.XML_IMAGE -> Image(parent, parser)
                    Button.XML_BUTTON -> Button(parent, parser)
                    Form.XML_FORM -> Form(parent, parser)
                    Input.XML_INPUT -> Input(parent, parser)
                    Link.XML_LINK -> Link(parent, parser)
                    else -> null
                }
                else -> null
            }
        }
    }
}
