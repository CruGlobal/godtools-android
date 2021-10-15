package org.cru.godtools.tool.lesson.util

import android.net.Uri
import java.util.Locale
import org.cru.godtools.base.HOST_GODTOOLSAPP_COM

// Sample Lesson deep link: https://godtoolsapp.com/lessons/lessonholyspirit/en

internal fun Uri.isLessonDeepLink() = ("http".equals(scheme, true) || "https".equals(scheme, true)) &&
    host.equals(HOST_GODTOOLSAPP_COM, true) &&
    pathSegments.getOrNull(0) == "lessons" && pathSegments.size >= 3

internal val Uri.lessonDeepLinkCode get() = pathSegments[1]?.takeIf { it.isNotEmpty() }
internal val Uri.lessonDeepLinkLocale
    get() = pathSegments[2]?.takeIf { it.isNotEmpty() }?.let { Locale.forLanguageTag(it) }
