package org.cru.godtools.util

import android.app.Activity
import org.cru.godtools.article.activity.startCategoriesActivity
import org.cru.godtools.model.Tool.Type
import org.cru.godtools.tract.activity.TractActivity
import org.cru.godtools.xml.service.ManifestManager
import java.util.Locale

fun Activity.openToolActivity(code: String, type: Type, vararg languages: Locale) {
    // launch activity based on the tool type
    when (type) {
        Type.TRACT -> {
            // start pre-loading the tract in the first language
            ManifestManager.getInstance(this).preloadLatestPublishedManifest(code, languages[0])
            TractActivity.start(this, code, *languages)
        }
        Type.ARTICLE -> startCategoriesActivity(code, languages[0])
    }
}
