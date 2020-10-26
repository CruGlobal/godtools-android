package org.cru.godtools.base.tool

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import java.util.Locale
import org.ccci.gto.android.common.util.os.putLocaleArray
import org.cru.godtools.base.EXTRA_LANGUAGES
import org.cru.godtools.base.EXTRA_TOOL

// region TractActivity
private const val ACTIVITY_CLASS_TRACT = "org.cru.godtools.tract.activity.TractActivity"

const val EXTRA_SHOW_TIPS = "org.cru.godtools.tract.activity.TractActivity.SHOW_TIPS"

const val SHORTCUT_LAUNCH = "org.cru.goodtools.tract.activity.TractActivity.SHORTCUT_LAUNCH"

fun Activity.startTractActivity(toolCode: String, vararg languages: Locale?, showTips: Boolean) =
    startActivity(createTractActivityIntent(toolCode, *languages, showTips = showTips))

fun Context.createTractActivityIntent(toolCode: String, vararg languages: Locale?, showTips: Boolean = false) =
    Intent().setClassName(this, ACTIVITY_CLASS_TRACT)
        .putExtra(EXTRA_TOOL, toolCode)
        .putLanguagesExtra(*languages)
        .putExtra(EXTRA_SHOW_TIPS, showTips)

private fun Intent.putLanguagesExtra(vararg languages: Locale?) = putExtras(Bundle().apply {
    // XXX: we use singleString mode to support using this intent for legacy shortcuts
    putLocaleArray(EXTRA_LANGUAGES, languages.filterNotNull().toTypedArray(), true)
})
// endregion TractActivity
