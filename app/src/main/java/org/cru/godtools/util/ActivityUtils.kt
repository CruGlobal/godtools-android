package org.cru.godtools.util

import android.app.Activity
import java.util.Locale
import org.cru.godtools.base.tool.startLessonActivity
import org.cru.godtools.base.tool.startTractActivity
import org.cru.godtools.base.ui.startArticlesActivity
import org.cru.godtools.model.Tool.Type

fun Activity.openToolActivity(code: String, type: Type, vararg languages: Locale, showTips: Boolean = false) =
    when (type) {
        Type.TRACT -> startTractActivity(code, *languages, showTips = showTips)
        Type.ARTICLE -> startArticlesActivity(code, languages[0])
        Type.LESSON -> startLessonActivity(code, languages[0])
        Type.UNKNOWN -> Unit
    }
