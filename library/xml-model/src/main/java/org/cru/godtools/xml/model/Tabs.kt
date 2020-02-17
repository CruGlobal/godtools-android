package org.cru.godtools.xml.model

import org.ccci.gto.android.common.util.XmlPullParserUtils
import org.cru.godtools.xml.Constants
import org.xmlpull.v1.XmlPullParser
import java.util.Collections

class Tabs internal constructor(parent: Base, parser: XmlPullParser) : Content(parent, parser) {
    companion object {
        internal const val XML_TABS = "tabs"
    }

    val tabs: List<Tab>

    init {
        parser.require(XmlPullParser.START_TAG, Constants.XMLNS_CONTENT, XML_TABS)

        // process any child elements
        val tabs = mutableListOf<Tab>()
        parsingChildren@ while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                Constants.XMLNS_CONTENT -> when (parser.name) {
                    Tab.XML_TAB -> {
                        tabs.add(Tab.fromXml(this, parser, tabs.size))
                        continue@parsingChildren
                    }
                }
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser)
        }
        this.tabs = Collections.unmodifiableList(tabs)
    }
}
