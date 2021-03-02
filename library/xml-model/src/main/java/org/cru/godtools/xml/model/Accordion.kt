package org.cru.godtools.xml.model

import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.XMLNS_CONTENT
import org.xmlpull.v1.XmlPullParser

private const val XML_SECTION = "section"
private const val XML_SECTION_HEADER = "header"

class Accordion : Content {
    companion object {
        internal const val XML_ACCORDION = "accordion"
    }

    val sections: List<Section>

    @OptIn(ExperimentalStdlibApi::class)
    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_ACCORDION)

        sections = buildList {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue

                when (parser.namespace) {
                    XMLNS_CONTENT -> when (parser.name) {
                        XML_SECTION -> add(Section(this@Accordion, parser))
                        else -> parser.skipTag()
                    }
                    else -> parser.skipTag()
                }
            }
        }
    }

    class Section : BaseModel, Parent {
        private val accordion: Accordion
        val id: String get() = "section-${accordion.sections.indexOf(this)}"

        val header: Text?
        override val content: List<Content>

        internal constructor(parent: Accordion, parser: XmlPullParser) : super(parent) {
            accordion = parent
            parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_SECTION)

            // process any child elements
            var header: Text? = null
            content = parseContent(parser) {
                when (parser.namespace) {
                    XMLNS_CONTENT -> when (parser.name) {
                        XML_SECTION_HEADER -> header = parser.parseTextChild(this, XMLNS_CONTENT, XML_SECTION_HEADER)
                    }
                }
            }
            this.header = header
        }
    }
}
