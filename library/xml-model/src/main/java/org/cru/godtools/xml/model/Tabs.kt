package org.cru.godtools.xml.model

import androidx.annotation.VisibleForTesting
import org.ccci.gto.android.common.util.XmlPullParserUtils
import org.cru.godtools.xml.Constants
import org.xmlpull.v1.XmlPullParser
import java.util.Collections

class Tabs : Content {
    companion object {
        internal const val XML_TABS = "tabs"
    }

    val tabs: List<Tab>

    @VisibleForTesting
    internal constructor(parent: Base, tabs: List<Tab>) : super(parent) {
        this.tabs = tabs
    }

    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, Constants.XMLNS_CONTENT, XML_TABS)

        // process any child elements
        val tabs = mutableListOf<Tab>()
        parsingChildren@ while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                Constants.XMLNS_CONTENT -> when (parser.name) {
                    Tab.XML_TAB -> {
                        tabs.add(Tab(this, tabs.size, parser))
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
