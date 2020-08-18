package org.cru.godtools.util

import android.app.Activity
import java.util.Locale
import org.cru.godtools.article.ui.categories.startCategoriesActivity
import org.cru.godtools.model.Tool.Type
import org.cru.godtools.tract.activity.startTractActivity

fun Activity.openToolActivity(code: String, type: Type, vararg languages: Locale, showTips: Boolean = false) =
    when (type) {
        Type.TRACT -> startTractActivity(code, *languages, showTips = showTips)
        Type.ARTICLE -> startCategoriesActivity(code, languages[0])
        Type.UNKNOWN -> Unit
    }
