package org.cru.godtools.xml.model.lesson

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import org.ccci.gto.android.common.util.xmlpull.skipTag
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.R
import org.cru.godtools.xml.model.BaseModel
import org.cru.godtools.xml.model.Content
import org.cru.godtools.xml.model.ImageGravity
import org.cru.godtools.xml.model.ImageScaleType
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Parent
import org.cru.godtools.xml.model.Styles
import org.cru.godtools.xml.model.XMLNS_LESSON
import org.cru.godtools.xml.model.XML_BACKGROUND_COLOR
import org.cru.godtools.xml.model.XML_BACKGROUND_IMAGE
import org.cru.godtools.xml.model.XML_BACKGROUND_IMAGE_GRAVITY
import org.cru.godtools.xml.model.XML_BACKGROUND_IMAGE_SCALE_TYPE
import org.cru.godtools.xml.model.XML_LISTENERS
import org.cru.godtools.xml.model.getAttributeValueAsColorOrNull
import org.cru.godtools.xml.model.getAttributeValueAsImageGravity
import org.cru.godtools.xml.model.getAttributeValueAsImageScaleTypeOrNull
import org.cru.godtools.xml.model.parseContent
import org.xmlpull.v1.XmlPullParser

private const val XML_PAGE = "page"
private const val XML_HIDDEN = "hidden"
private const val XML_CONTENT = "content"

@ColorInt
private const val DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT
private val DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ImageScaleType.FILL_X
private val DEFAULT_BACKGROUND_IMAGE_GRAVITY = ImageGravity.CENTER

class LessonPage : BaseModel, Parent, Styles {
    val id get() = fileName ?: "${manifest.code}-$position"
    val position: Int

    @VisibleForTesting
    internal val fileName: String?

    val isHidden: Boolean
    val listeners: Set<Event.Id>

    @ColorInt
    val backgroundColor: Int
    @VisibleForTesting
    internal val _backgroundImage: String?
    val backgroundImage get() = getResource(_backgroundImage)
    val backgroundImageGravity: ImageGravity
    val backgroundImageScaleType: ImageScaleType

    @ColorInt
    private val _controlColor: Int?
    @get:ColorInt
    val controlColor get() = _controlColor ?: manifest.lessonControlColor

    override val textSize get() = R.dimen.text_size_lesson

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

        isHidden = parser.getAttributeValue(null, XML_HIDDEN)?.toBoolean() ?: false
        listeners = parseEvents(parser, XML_LISTENERS)

        backgroundColor = parser.getAttributeValueAsColorOrNull(XML_BACKGROUND_COLOR) ?: DEFAULT_BACKGROUND_COLOR
        _backgroundImage = parser.getAttributeValue(null, XML_BACKGROUND_IMAGE)
        backgroundImageGravity =
            parser.getAttributeValueAsImageGravity(XML_BACKGROUND_IMAGE_GRAVITY, DEFAULT_BACKGROUND_IMAGE_GRAVITY)
        backgroundImageScaleType = parser.getAttributeValueAsImageScaleTypeOrNull(XML_BACKGROUND_IMAGE_SCALE_TYPE)
            ?: DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE

        _controlColor = parser.getAttributeValueAsColorOrNull(XML_CONTROL_COLOR)

        val content = mutableListOf<Content>()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.namespace) {
                XMLNS_LESSON -> when (parser.name) {
                    XML_CONTENT -> content += parseContent(parser)
                    else -> parser.skipTag()
                }
                else -> parser.skipTag()
            }
        }
        this.content = content
    }
}

@get:ColorInt
val LessonPage?.backgroundColor get() = this?.backgroundColor ?: DEFAULT_BACKGROUND_COLOR
val LessonPage?.backgroundImageScaleType get() = this?.backgroundImageScaleType ?: DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE
val LessonPage?.backgroundImageGravity get() = this?.backgroundImageGravity ?: DEFAULT_BACKGROUND_IMAGE_GRAVITY

@get:ColorInt
val LessonPage?.controlColor get() = this?.controlColor ?: DEFAULT_LESSON_CONTROL_COLOR
