package org.cru.godtools.article.aem.service.support

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import org.cru.godtools.article.aem.model.Article
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Test

class HtmlParserTest {
    @Test
    fun extractUrls() {
        val article = Article(Uri.parse("https://example.com/path/")).apply {
            content = loadHtml("tests/HtmlParser/extract_urls.html")
        }
        val urls = extractResources(article).map { it.uri }
        assertThat(
            urls, containsInAnyOrder(
                Uri.parse("http://cdn2-www.cru.org/clientlibs/A.main.min.css.cf.yZHv2yPfJ7.css"),
                Uri.parse("https://cdn2-www.cru.org/clientlibs/A.main.min.css.cf.yZHv2yPfJ7.css"),
                Uri.parse("https://example.com/path/A.main.min.css.pagespeed.cf.yZHv2yPfJ7.css"),
                Uri.parse("https://example.com/A.main.min.css.pagespeed.cf.yZHv2yPfJ7.css"),
                Uri.parse("https://cdn1-www.cru.org/_clientlibs-godtools.min.css.pagespeed.cc.DAFZbcbAT3.css"),
                Uri.parse("http://cdn1-www.cru.org/experience-fragments/prayers3.jpeg.pagespeed.ce.HQcV4MdwSA.jpg"),
                Uri.parse("https://cdn1-www.cru.org/experience-fragments/prayers3.jpeg.pagespeed.ce.HQcV4MdwSA.jpg"),
                Uri.parse("https://example.com/path/prayers3.jpeg.pagespeed.ce.HQcV4MdwSA.jpg"),
                Uri.parse("https://example.com/prayers3.jpeg.pagespeed.ce.HQcV4MdwSA.jpg")
            )
        )
    }

    private fun loadHtml(file: String): String =
        InstrumentationRegistry.getInstrumentation().context.assets.open(file).bufferedReader().use { it.readText() }
}
