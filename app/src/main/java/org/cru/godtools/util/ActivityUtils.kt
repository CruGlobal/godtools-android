package org.cru.godtools.util

import android.app.Activity
import org.cru.godtools.article.activity.startCategoriesActivity
import org.cru.godtools.model.Tool.Type
import org.cru.godtools.tract.activity.TractActivity
import org.cru.godtools.xml.service.ManifestManager
import java.util.Locale

fun openToolActivity(context: Activity, code: String, type: Type, vararg languages: Locale) {
    // launch activity based on the tool type
    when (type) {
        Type.TRACT -> {
            // start pre-loading the tract in the first language
            ManifestManager.getInstance(context).getLatestPublishedManifest(code, languages[0])
            TractActivity.start(context, code, *languages)
        }
        Type.ARTICLE -> context.startCategoriesActivity(code, languages[0])
    }
}