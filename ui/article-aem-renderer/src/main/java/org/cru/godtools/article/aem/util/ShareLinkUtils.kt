package org.cru.godtools.article.aem.util

import androidx.annotation.AnyThread
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import org.cru.godtools.article.aem.model.Article

object ShareLinkUtils {
    @AnyThread
    fun articleShareLinkBuilder(article: Article?): DynamicLink.Builder? {
        return article?.canonicalUri?.buildUpon()
            ?.appendQueryParameter("icid", "gtshare")
            ?.build()?.let {
                FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setDomainUriPrefix("https://godtools.page.link")
                    .setLink(it)
//                    // Open links with this app on Android
//                    .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
//                    // Open links with com.example.ios on iOS
//                    .setIosParameters(DynamicLink.IosParameters.Builder("com.example.ios").build())
            }
    }
}
