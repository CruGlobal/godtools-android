package org.cru.godtools.xml.model

import androidx.annotation.VisibleForTesting
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.XMLNS_CONTENT
import org.xmlpull.v1.XmlPullParser

class Tabs : Content {
    companion object {
        internal const val XML_TABS = "tabs"
    }

    val tabs: List<Tab>
    override val tips get() = tabs.flatMap { it.tips }

    @VisibleForTesting
    internal constructor(parent: Base, tabs: List<Tab>) : super(parent) {
        this.tabs = tabs
    }

    @OptIn(ExperimentalStdlibApi::class)
    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_TABS)

        tabs = buildList {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue

                when (parser.namespace) {
                    XMLNS_CONTENT -> when (parser.name) {
                        Tab.XML_TAB -> add(Tab(this@Tabs, size, parser))
                        else -> parser.skipTag()
                    }
                    else -> parser.skipTag()
                }
            }
        }
    }
}
