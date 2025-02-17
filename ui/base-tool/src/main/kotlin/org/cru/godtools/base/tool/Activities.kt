package org.cru.godtools.base.tool

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import java.util.Locale
import org.ccci.gto.android.common.util.os.putLocale
import org.cru.godtools.base.EXTRA_LANGUAGE
import org.cru.godtools.base.EXTRA_TOOL

const val SHORTCUT_LAUNCH = "org.cru.godtools.tool.SHORTCUT_LAUNCH"

// region LessonActivity
private const val ACTIVITY_CLASS_LESSON = "org.cru.godtools.tool.lesson.ui.LessonActivity"
const val EXTRA_RESUME_PAGE = "resume_page_id"

fun Activity.startLessonActivity(toolCode: String, language: Locale, resumePageId: String? = null) =
    startActivity(createLessonActivityIntent(toolCode, language, resumePageId))

fun Context.createLessonActivityIntent(toolCode: String, language: Locale, resumePageId: String? = null) =
    Intent().setClassName(this, ACTIVITY_CLASS_LESSON)
        .putExtra(EXTRA_TOOL, toolCode)
        .putExtras(Bundle().apply { putLocale(EXTRA_LANGUAGE, language) })
        .putExtra(EXTRA_RESUME_PAGE, resumePageId)
// endregion LessonActivity
