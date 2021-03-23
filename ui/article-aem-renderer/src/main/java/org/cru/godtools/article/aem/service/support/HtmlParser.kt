package org.cru.godtools.article.aem.service.support

import android.net.Uri
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.model.Resource
import org.jsoup.Jsoup

fun Article.extractResources(): List<Resource> {
    val doc = Jsoup.parse(content, uri.toString())
    val css = doc.select("link[rel='stylesheet']").eachAttr("abs:href").map { Uri.parse(it) }
    val imgs = doc.select("img").eachAttr("abs:src").map { Uri.parse(it) }
    return (css + imgs).map { Resource(it) }
}
