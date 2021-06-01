package org.cru.godtools.xml.model

import org.xmlpull.v1.XmlPullParser

interface Parent : Base {
    val content: List<Content>
}

internal inline val Parent.contentTips get() = content.flatMap { it.tips }

/**
 * @param block Custom parsing logic, if the block processes the current tag,
 * it should advance the parser to the END_TAG event.
 */
@OptIn(ExperimentalStdlibApi::class)
internal inline fun Parent.parseContent(
    parser: XmlPullParser,
    block: () -> Unit = { }
) = buildList {
    parser.require(XmlPullParser.START_TAG, null, null)

    while (parser.next() != XmlPullParser.END_TAG) {
        if (parser.eventType != XmlPullParser.START_TAG) continue

        // execute any custom parsing logic from the call-site
        // if the block consumes the tag, the parser will be on an END_TAG after returning
        block()
        if (parser.eventType == XmlPullParser.END_TAG) continue

        // try parsing this child element as a content node
        Content.fromXml(this@parseContent, parser)?.takeUnless { it.isIgnored }?.let { add(it) }
    }
}
