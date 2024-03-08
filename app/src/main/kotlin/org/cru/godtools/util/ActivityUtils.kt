package org.cru.godtools.util

import android.app.Activity
import android.content.Context
import java.util.Locale
import org.cru.godtools.base.tool.createLessonActivityIntent
import org.cru.godtools.base.tool.startLessonActivity
import org.cru.godtools.base.ui.createArticlesIntent
import org.cru.godtools.base.ui.createCyoaActivityIntent
import org.cru.godtools.base.ui.createTractActivityIntent
import org.cru.godtools.base.ui.startArticlesActivity
import org.cru.godtools.base.ui.startCyoaActivity
import org.cru.godtools.base.ui.startTractActivity
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Tool.Type

fun Activity.openToolActivity(code: String, type: Type, vararg languages: Locale, showTips: Boolean = false) =
    when (type) {
        Type.TRACT -> startTractActivity(code, *languages, showTips = showTips)
        Type.ARTICLE -> startArticlesActivity(code, languages[0])
        Type.CYOA -> startCyoaActivity(code, *languages)
        Type.LESSON -> startLessonActivity(code, languages[0])
        Type.META, Type.UNKNOWN -> Unit
    }

fun Tool.createToolIntent(context: Context, languages: List<Locale>, showTips: Boolean = false) = code?.let { code ->
    when (type) {
        Type.TRACT -> context.createTractActivityIntent(code, *languages.toTypedArray(), showTips = showTips)
        Type.ARTICLE -> context.createArticlesIntent(code, languages[0])
        Type.CYOA -> context.createCyoaActivityIntent(code, *languages.toTypedArray())
        Type.LESSON -> context.createLessonActivityIntent(code, languages[0])
        Type.META, Type.UNKNOWN -> null
    }
}
