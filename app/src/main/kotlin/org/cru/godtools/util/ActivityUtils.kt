package org.cru.godtools.util

import android.app.Activity
import android.content.Context
import android.content.Intent
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
        Type.CYOA -> startCyoaActivity(code, *languages, showTips = showTips)
        Type.LESSON -> startLessonActivity(code, languages[0])
        Type.META, Type.UNKNOWN -> Unit
    }

fun Context.createToolIntent(
    tool: Tool,
    languages: List<Locale>,
    activeLocale: Locale? = null,
    showTips: Boolean = false,
    saveLanguageSettings: Boolean = false,
    resumeProgress: Boolean = false,
) = createToolIntent(
    type = tool.type,
    toolCode = tool.code,
    languages = languages,
    activeLocale = activeLocale,
    showTips = showTips,
    saveLanguageSettings = saveLanguageSettings,
    resumeProgress = resumeProgress,
    progressLastPageId = tool.progressLastPageId,
)

fun Context.createToolIntent(
    type: Type,
    toolCode: String?,
    languages: List<Locale>,
    activeLocale: Locale? = null,
    showTips: Boolean = false,
    saveLanguageSettings: Boolean = false,
    resumeProgress: Boolean = false,
    progressLastPageId: String? = null,
): Intent? {
    val toolCode = toolCode ?: return null
    if (languages.isEmpty()) return null

    return when (type) {
        Type.ARTICLE -> createArticlesIntent(toolCode, languages[0])

        Type.CYOA -> createCyoaActivityIntent(
            toolCode,
            *languages.toTypedArray(),
            saveLanguageSettings = saveLanguageSettings
        )

        Type.LESSON -> createLessonActivityIntent(
            toolCode,
            languages[0],
            resumePageId = progressLastPageId.takeIf { resumeProgress }
        )

        Type.TRACT -> createTractActivityIntent(
            toolCode,
            *languages.toTypedArray(),
            activeLocale = activeLocale,
            showTips = showTips,
            saveLanguageSettings = saveLanguageSettings,
        )

        Type.META, Type.UNKNOWN -> null
    }
}
