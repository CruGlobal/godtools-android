package org.cru.godtools.base.ui

import android.app.Activity
import android.app.ActivityOptions
import android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import java.util.Locale
import org.ccci.gto.android.common.util.os.putLocale
import org.ccci.gto.android.common.util.os.putLocaleArray
import org.cru.godtools.base.EXTRA_ACTIVE_LOCALE
import org.cru.godtools.base.EXTRA_LANGUAGE
import org.cru.godtools.base.EXTRA_LANGUAGES
import org.cru.godtools.base.EXTRA_PAGE
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.dashboard.Page

const val EXTRA_SHOW_TIPS = "org.cru.godtools.base.tool.activity.MultiLanguageToolActivity.SHOW_TIPS"
const val EXTRA_SAVE_LANGUAGE_SETTINGS =
    "org.cru.godtools.base.tool.activity.MultiLanguageToolActivity.SAVE_LANGUAGE_SETTINGS"

// region DashboardActivity
private const val ACTIVITY_CLASS_DASHBOARD = "org.cru.godtools.ui.dashboard.DashboardActivity"

fun Activity.startDashboardActivity(page: Page? = null) = startActivity(createDashboardIntent(page))

fun Context.createDashboardIntent(page: Page?) = Intent().setClassName(this, ACTIVITY_CLASS_DASHBOARD)
    .putExtra(EXTRA_PAGE, page)
// endregion DashboardActivity

// region AppLanguageActivity
private const val ACTIVITY_CLASS_APP_LANGUAGE = "org.cru.godtools.ui.languages.app.AppLanguageActivity"

fun Context.startAppLanguageActivity() = startActivity(
    Intent()
        .setClassName(this, ACTIVITY_CLASS_APP_LANGUAGE)
        .putExtras(BaseActivity.buildExtras(this))
)
// endregion AppLanguageActivity

// region ArticlesActivity
private const val ACTIVITY_CLASS_ARTICLES = "org.cru.godtools.article.ui.ArticlesActivity"

fun Activity.startArticlesActivity(toolCode: String, language: Locale) =
    startActivity(createArticlesIntent(toolCode, language))

fun Context.createArticlesIntent(toolCode: String, language: Locale) =
    Intent().setClassName(this, ACTIVITY_CLASS_ARTICLES)
        .putExtras(buildToolExtras(toolCode, language))
// endregion ArticlesActivity

// region CyoaActivity
private const val ACTIVITY_CLASS_CYOA = "org.cru.godtools.tool.cyoa.ui.CyoaActivity"

fun Activity.startCyoaActivity(toolCode: String, vararg languages: Locale?, showTips: Boolean = false) =
    startActivity(createCyoaActivityIntent(toolCode, *languages, showTips = showTips))

fun Context.createCyoaActivityIntent(
    toolCode: String,
    vararg languages: Locale?,
    pageId: String? = null,
    showTips: Boolean = false,
    saveLanguageSettings: Boolean = false,
) = Intent().setClassName(this, ACTIVITY_CLASS_CYOA)
    .putExtra(EXTRA_TOOL, toolCode)
    .putLanguagesExtra(*languages)
    .putExtra(EXTRA_PAGE, pageId)
    .putExtra(EXTRA_SHOW_TIPS, showTips)
    .putExtra(EXTRA_SAVE_LANGUAGE_SETTINGS, saveLanguageSettings)
// endregion CyoaActivity

// region TractActivity
private const val ACTIVITY_CLASS_TRACT = "org.cru.godtools.tract.activity.TractActivity"

fun Activity.startTractActivity(toolCode: String, vararg languages: Locale?, showTips: Boolean) =
    startActivity(createTractActivityIntent(toolCode, *languages, showTips = showTips))

fun Context.createTractActivityIntent(
    toolCode: String,
    vararg languages: Locale?,
    activeLocale: Locale? = null,
    page: Int = 0,
    showTips: Boolean = false,
    saveLanguageSettings: Boolean = false,
) = Intent().setClassName(this, ACTIVITY_CLASS_TRACT)
    .putExtra(EXTRA_TOOL, toolCode)
    .putLanguagesExtra(*languages)
    .putExtra(EXTRA_ACTIVE_LOCALE, activeLocale)
    .putExtra(EXTRA_PAGE, page)
    .putExtra(EXTRA_SHOW_TIPS, showTips)
    .putExtra(EXTRA_SAVE_LANGUAGE_SETTINGS, saveLanguageSettings)
// endregion TractActivity

fun Context.buildToolExtras(toolCode: String, language: Locale) = BaseActivity.buildExtras(this).apply {
    putString(EXTRA_TOOL, toolCode)
    putLocale(EXTRA_LANGUAGE, language)
}

private fun Intent.putLanguagesExtra(vararg languages: Locale?) = putExtras(
    // HACK: we use singleString mode to support using this intent for legacy shortcuts.
    //       This is required for API 21 and may be required for other versions of the API.
    //       Test case: disable singleString mode, and add a shortcut for a tool with both primary & parallel languages.
    //       If the shortcut fails to open then singleString mode is still required
    Bundle().apply { putLocaleArray(EXTRA_LANGUAGES, languages.filterNotNull().toTypedArray(), true) }
)

// region QRCodeActivity
private const val ACTIVITY_CLASS_QR_CODE = "org.cru.godtools.qrcode.activity.QRCodeActivity"
const val EXTRA_SHARE_URL = "EXTRA_SHARE_URL"

fun Context.createQrCodeActivityIntent(data: String?): Intent = Intent().setClassName(this, ACTIVITY_CLASS_QR_CODE)
    .putExtra(EXTRA_SHARE_URL, data)

fun Context.createQrCodePendingIntent(data: String): PendingIntent = PendingIntent.getActivity(
    this,
    0,
    createQrCodeActivityIntent(data),
    PendingIntent.FLAG_IMMUTABLE,
    ActivityOptions.makeBasic()
        .apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                setPendingIntentCreatorBackgroundActivityStartMode(MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
            }
        }
        .toBundle()
)
// endregion QRCodeActivity
