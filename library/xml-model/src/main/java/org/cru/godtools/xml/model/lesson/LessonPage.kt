package org.cru.godtools.xml.model.lesson

import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.xml.XMLNS_LESSON
import org.cru.godtools.xml.model.BaseModel
import org.cru.godtools.xml.model.Content
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Parent
import org.cru.godtools.xml.model.parseContent
import org.xmlpull.v1.XmlPullParser

private const val XML_PAGE = "page"
private const val XML_CONTENT = "content"

class LessonPage : BaseModel, Parent {
    val id get() = fileName ?: "${manifest.code}-$position"
    val position: Int

    @VisibleForTesting
    internal val fileName: String?

    override val content: List<Content>

    @WorkerThread
    internal constructor(
        manifest: Manifest,
        position: Int,
        fileName: String?,
        parser: XmlPullParser
    ) : super(manifest) {
        this.fileName = fileName
        this.position = position

        parser.require(XmlPullParser.START_TAG, XMLNS_LESSON, XML_PAGE)

        var content: List<Content>? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                XMLNS_LESSON -> when (parser.name) {
                    XML_CONTENT -> content = parseContent(parser)
                }
            }

            parser.skipTag()
        }
        this.content = content.orEmpty()
    }
}
