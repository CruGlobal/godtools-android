package org.cru.godtools.xml.model

import org.xmlpull.v1.XmlPullParser

interface Parent : BaseModel {
    val content: List<Content>
}

@OptIn(ExperimentalStdlibApi::class)
internal inline fun Parent.parseContent(
    parser: XmlPullParser,
    block: () -> Unit = { }
) = buildList {
    parser.require(XmlPullParser.START_TAG, null, null)

    while (parser.next() != XmlPullParser.END_TAG) {
        if (parser.eventType != XmlPullParser.START_TAG) continue

        // execute any custom parsing logic from the call-site
        block()
        if (parser.eventType == XmlPullParser.END_TAG) continue

        // try parsing this child element as a content node
        Content.fromXml(this@parseContent, parser, true)?.takeUnless { it.isIgnored }?.let { add(it) }
    }
}
