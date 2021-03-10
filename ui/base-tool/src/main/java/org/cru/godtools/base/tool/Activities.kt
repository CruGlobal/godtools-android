package org.cru.godtools.base.tool

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import java.util.Locale
import org.ccci.gto.android.common.util.os.putLocale
import org.ccci.gto.android.common.util.os.putLocaleArray
import org.cru.godtools.base.EXTRA_LANGUAGE
import org.cru.godtools.base.EXTRA_LANGUAGES
import org.cru.godtools.base.EXTRA_TOOL

const val SHORTCUT_LAUNCH = "org.cru.godtools.tool.SHORTCUT_LAUNCH"

// region LessonActivity
private const val ACTIVITY_CLASS_LESSON = "org.cru.godtools.tool.lesson.ui.LessonActivity"

fun Activity.startLessonActivity(toolCode: String, language: Locale) =
    startActivity(createLessonActivityIntent(toolCode, language))

fun Context.createLessonActivityIntent(toolCode: String, language: Locale) =
    Intent().setClassName(this, ACTIVITY_CLASS_LESSON)
        .putExtra(EXTRA_TOOL, toolCode)
        .putExtras(Bundle().apply { putLocale(EXTRA_LANGUAGE, language) })
// endregion LessonActivity

// region TractActivity
private const val ACTIVITY_CLASS_TRACT = "org.cru.godtools.tract.activity.TractActivity"

const val EXTRA_SHOW_TIPS = "org.cru.godtools.tract.activity.TractActivity.SHOW_TIPS"

fun Activity.startTractActivity(toolCode: String, vararg languages: Locale?, showTips: Boolean) =
    startActivity(createTractActivityIntent(toolCode, *languages, showTips = showTips))

fun Context.createTractActivityIntent(toolCode: String, vararg languages: Locale?, showTips: Boolean = false) =
    Intent().setClassName(this, ACTIVITY_CLASS_TRACT)
        .putExtra(EXTRA_TOOL, toolCode)
        .putLanguagesExtra(*languages)
        .putExtra(EXTRA_SHOW_TIPS, showTips)

private fun Intent.putLanguagesExtra(vararg languages: Locale?) = putExtras(
    // XXX: we use singleString mode to support using this intent for legacy shortcuts
    Bundle().apply { putLocaleArray(EXTRA_LANGUAGES, languages.filterNotNull().toTypedArray(), true) }
)
// endregion TractActivity
