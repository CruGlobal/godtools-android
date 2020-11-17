package org.cru.godtools.xml.model

import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.XMLNS_CONTENT
import org.cru.godtools.xml.XMLNS_TRAINING
import org.cru.godtools.xml.model.tips.InlineTip
import org.cru.godtools.xml.model.tips.Tip
import org.xmlpull.v1.XmlPullParser

private const val XML_RESTRICT_TO = "restrictTo"

abstract class Content : BaseModel {
    private val restrictTo: Set<DeviceType>

    protected constructor(parent: Base) : super(parent) {
        restrictTo = DeviceType.ALL
    }

    protected constructor(parent: Base, parser: XmlPullParser) : super(parent) {
        restrictTo = parser.getAttributeValueAsDeviceTypesOrNull(XML_RESTRICT_TO) ?: DeviceType.ALL
    }

    /**
     * @return true if this content element should be completely ignored.
     */
    val isIgnored get() = !restrictTo.contains(DeviceType.ANDROID) && !restrictTo.contains(DeviceType.MOBILE)

    open val tips get() = emptyList<Tip>()

    companion object {
        internal fun fromXml(
            parent: Base,
            parser: XmlPullParser,
            consumeUnrecognizedTags: Boolean = false
        ): Content? {
            parser.require(XmlPullParser.START_TAG, null, null)

            return when (parser.namespace) {
                XMLNS_CONTENT -> when (parser.name) {
                    Paragraph.XML_PARAGRAPH -> when (parser.getAttributeValue(null, Paragraph.XML_FALLBACK)
                        ?.toBoolean()) {
                        true -> Fallback(parent, parser)
                        else -> Paragraph(parent, parser)
                    }
                    Tabs.XML_TABS -> Tabs(parent, parser)
                    Text.XML_TEXT -> Text(parent, parser)
                    Image.XML_IMAGE -> Image(parent, parser)
                    Button.XML_BUTTON -> Button(parent, parser)
                    Fallback.XML_FALLBACK -> Fallback(parent, parser)
                    Form.XML_FORM -> Form(parent, parser)
                    Input.XML_INPUT -> Input(parent, parser)
                    Link.XML_LINK -> Link(parent, parser)
                    else -> {
                        if (consumeUnrecognizedTags) parser.skipTag()
                        null
                    }
                }
                XMLNS_TRAINING -> when (parser.name) {
                    InlineTip.XML_TIP -> InlineTip(parent, parser)
                    else -> {
                        if (consumeUnrecognizedTags) parser.skipTag()
                        null
                    }
                }
                else -> {
                    if (consumeUnrecognizedTags) parser.skipTag()
                    null
                }
            }
        }
    }
}
