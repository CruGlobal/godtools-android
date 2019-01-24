package org.cru.godtools.article.aem.util

import android.net.Uri
import androidx.annotation.AnyThread
import org.cru.godtools.article.aem.model.Article

@AnyThread
fun Article.buildShareLink(): Uri? {
    return canonicalUri?.buildUpon()
        ?.appendQueryParameter("icid", "gtshare")
        ?.build()
}
