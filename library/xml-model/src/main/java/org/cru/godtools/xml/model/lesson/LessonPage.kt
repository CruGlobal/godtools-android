package org.cru.godtools.xml.model.lesson

import android.graphics.Color
import androidx.annotation.ColorInt
import org.cru.godtools.tool.model.lesson.backgroundImageGravity
import org.cru.godtools.xml.model.ImageScaleType

@ColorInt
private const val DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT
private val DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ImageScaleType.FILL_X

@Deprecated("Use kotlin mpp library instead")
typealias LessonPage = org.cru.godtools.tool.model.lesson.LessonPage

@get:ColorInt
val LessonPage?.backgroundColor get() = this?.backgroundColor ?: DEFAULT_BACKGROUND_COLOR
val LessonPage?.backgroundImageScaleType get() = this?.backgroundImageScaleType ?: DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE
@Deprecated(
    "Use kotlin mpp library instead",
    ReplaceWith("backgroundImageGravity", "org.cru.godtools.tool.model.lesson.backgroundImageGravity")
)
inline val LessonPage?.backgroundImageGravity get() = backgroundImageGravity

@get:ColorInt
val LessonPage?.controlColor get() = this?.controlColor ?: DEFAULT_LESSON_CONTROL_COLOR
