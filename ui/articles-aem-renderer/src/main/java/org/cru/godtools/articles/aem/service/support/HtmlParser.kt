package org.cru.godtools.articles.aem.service.support

import android.net.Uri
import org.cru.godtools.articles.aem.model.Article
import org.cru.godtools.articles.aem.model.Resource
import org.jsoup.Jsoup

fun extractResources(article: Article): List<Resource> {
    val doc = Jsoup.parse(article.content, article.uri.toString())
    val css = doc.select("link[rel='stylesheet']").eachAttr("abs:href").map { Uri.parse(it) }
    val imgs = doc.select("img").eachAttr("abs:src").map { Uri.parse(it) }
    return (css + imgs).map { Resource(it) }
}
