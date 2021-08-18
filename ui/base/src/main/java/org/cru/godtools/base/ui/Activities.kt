package org.cru.godtools.base.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import java.util.Locale
import org.ccci.gto.android.common.util.os.putLocale
import org.cru.godtools.base.EXTRA_LANGUAGE
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.ui.activity.BaseActivity

// region ArticlesActivity
private const val ACTIVITY_CLASS_ARTICLES = "org.cru.godtools.article.ui.ArticlesActivity"

fun Activity.startArticlesActivity(toolCode: String, language: Locale) =
    startActivity(createArticlesIntent(toolCode, language))

fun Context.createArticlesIntent(toolCode: String, language: Locale) =
    Intent().setClassName(this, ACTIVITY_CLASS_ARTICLES)
        .putExtras(buildToolExtras(toolCode, language))
// endregion ArticlesActivity

fun Context.buildToolExtras(toolCode: String?, language: Locale?) = BaseActivity.buildExtras(this).apply {
    putString(EXTRA_TOOL, toolCode)
    putLocale(EXTRA_LANGUAGE, language)
}
