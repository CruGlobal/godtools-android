package org.cru.godtools.tool.lesson.analytics.appsflyer

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.util.Locale
import org.cru.godtools.analytics.appsflyer.AppsFlyerDeepLinkResolver
import org.cru.godtools.base.tool.createLessonActivityIntent
import org.cru.godtools.tool.lesson.ui.LessonActivity
import org.cru.godtools.tool.lesson.util.isLessonDeepLink

internal object LessonAppsFlyerDeepLinkResolver : AppsFlyerDeepLinkResolver {
    override fun resolve(context: Context, uri: Uri?, data: Map<String, String?>) = when {
        uri?.isLessonDeepLink() == true -> Intent(Intent.ACTION_VIEW, uri, context, LessonActivity::class.java)
        else -> null
    }

    override fun resolve(context: Context, deepLinkValue: String) = deepLinkValue.split("|")
        .takeIf { it.size >= 4 && it[0] == "tool" && it[1] == "lesson" }
        ?.let { context.createLessonActivityIntent(it[2], Locale.forLanguageTag(it[3])) }
}
