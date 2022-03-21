package org.cru.godtools.tool.lesson.analytics.appsflyer

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.cru.godtools.analytics.appsflyer.AppsFlyerDeepLinkResolver
import org.cru.godtools.tool.lesson.ui.LessonActivity
import org.cru.godtools.tool.lesson.util.isLessonDeepLink

internal object LessonAppsFlyerDeepLinkResolver : AppsFlyerDeepLinkResolver {
    override fun resolve(context: Context, uri: Uri?, data: Map<String, String?>) = when {
        uri?.isLessonDeepLink() == true -> Intent(Intent.ACTION_VIEW, uri, context, LessonActivity::class.java)
        else -> null
    }
}
