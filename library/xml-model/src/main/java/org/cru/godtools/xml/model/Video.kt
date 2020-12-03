package org.cru.godtools.xml.model

import androidx.annotation.WorkerThread
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.XMLNS_CONTENT
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser.START_TAG

private const val XML_VIDEO_ID = "videoId"
private const val XML_PROVIDER = "provider"
private const val XML_PROVIDER_YOUTUBE = "youtube"

class Video : Content {
    companion object {
        internal const val XML_VIDEO = "video"
    }

    enum class Provider {
        YOUTUBE, UNKNOWN;

        companion object {
            internal val DEFAULT = UNKNOWN

            internal fun parseOrNull(value: String?) = when (value) {
                XML_PROVIDER_YOUTUBE -> YOUTUBE
                else -> null
            }
        }
    }

    val provider: Provider
    val videoId: String?

    override val isIgnored get() = super.isIgnored || provider == Provider.UNKNOWN || videoId == null

    @WorkerThread
    internal constructor(parent: Base, parser: XmlPullParser) : super(parent, parser) {
        parser.require(START_TAG, XMLNS_CONTENT, XML_VIDEO)

        provider = Provider.parseOrNull(parser.getAttributeValue(null, XML_PROVIDER)) ?: Provider.DEFAULT
        videoId = parser.getAttributeValue(null, XML_VIDEO_ID)

        parser.skipTag()
    }
}
