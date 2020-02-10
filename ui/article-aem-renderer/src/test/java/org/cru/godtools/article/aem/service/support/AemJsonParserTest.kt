package org.cru.godtools.article.aem.service.support

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AemJsonParserTest {
    @Test
    fun verifyArticleParseLogic() {
        loadJson("AemJsonParserTest-article-test.json")
            .findAemArticles(Uri.parse("https://stage.cru.org/content/experience-fragments/questions_about_god"))
            .toList()
            .run {
                assertThat(this, hasSize(2))
            }
    }

    private fun loadJson(file: String) =
        AemJsonParserTest::class.java.getResourceAsStream(file)!!.bufferedReader().use { it.readText() }
            .let { JSONObject(it) }
}
