package org.cru.godtools.xml.model

import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.model.tips.InlineTip
import org.cru.godtools.xml.model.tips.Tip
import org.xmlpull.v1.XmlPullParser

private const val XML_RESTRICT_TO = "restrictTo"
private const val XML_VERSION = "version"

abstract class Content : BaseModel {
    private val version: Int
    private val restrictTo: Set<DeviceType>

    internal constructor(
        parent: Base,
        version: Int = SUPPORTED_VERSION,
        restrictTo: Set<DeviceType>? = null
    ) : super(parent) {
        this.version = version
        this.restrictTo = restrictTo ?: DeviceType.ALL
    }

    protected constructor(parent: Base, parser: XmlPullParser) : super(parent) {
        version = parser.getAttributeValue(null, XML_VERSION)?.toIntOrNull() ?: SUPPORTED_VERSION
        restrictTo = parser.getAttributeValueAsDeviceTypesOrNull(XML_RESTRICT_TO) ?: DeviceType.ALL
    }

    /**
     * @return true if this content element should be completely ignored.
     */
    open val isIgnored get() = version > SUPPORTED_VERSION || restrictTo.none { it in DeviceType.SUPPORTED }

    open val tips get() = emptyList<Tip>()

    companion object {
        internal fun fromXml(parent: Base, parser: XmlPullParser): Content? {
            parser.require(XmlPullParser.START_TAG, null, null)

            return when (parser.namespace) {
                XMLNS_CONTENT -> when (parser.name) {
                    Paragraph.XML_PARAGRAPH ->
                        when (parser.getAttributeValue(null, Paragraph.XML_FALLBACK)?.toBoolean()) {
                            true -> Fallback(parent, parser)
                            else -> Paragraph(parent, parser)
                        }
                    Accordion.XML_ACCORDION -> Accordion(parent, parser)
                    Tabs.XML_TABS -> Tabs(parent, parser)
                    Text.XML_TEXT -> Text(parent, parser)
                    Image.XML_IMAGE -> Image(parent, parser)
                    Video.XML_VIDEO -> Video(parent, parser)
                    Animation.XML_ANIMATION -> Animation(parent, parser)
                    Button.XML_BUTTON -> Button(parent, parser)
                    Fallback.XML_FALLBACK -> Fallback(parent, parser)
                    Form.XML_FORM -> Form(parent, parser)
                    Input.XML_INPUT -> Input(parent, parser)
                    Link.XML_LINK -> Link(parent, parser)
                    Spacer.XML_SPACER -> Spacer(parent, parser)
                    else -> {
                        parser.skipTag()
                        null
                    }
                }
                XMLNS_TRAINING -> when (parser.name) {
                    InlineTip.XML_TIP -> InlineTip(parent, parser)
                    else -> {
                        parser.skipTag()
                        null
                    }
                }
                else -> {
                    parser.skipTag()
                    null
                }
            }
        }
    }
}
