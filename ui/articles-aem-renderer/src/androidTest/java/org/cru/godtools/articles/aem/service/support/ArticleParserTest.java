package org.cru.godtools.articles.aem.service.support;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import org.ccci.gto.android.common.util.IOUtils;
import org.cru.godtools.articles.aem.model.Article;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ArticleParserTest {
    @Test
    public void verifyArticleParseLogic() throws Exception {
        final JSONObject jsonObject = loadJson("tests/article-test.json");

        final List<Article> articles = ArticleParser.parse(Uri.parse(
                "https://stage.cru.org/content/experience-fragments/questions_about_god/english"), jsonObject);
        assertThat(articles.size(), is(2));
        for (final Article article : articles) {
            assertThat(article.parsedAttachments.size(), is(3));
        }
    }

    private JSONObject loadJson(@NonNull final String file) throws Exception {
        final InputStream data = getInstrumentation().getContext().getAssets().open(file);
        return new JSONObject(IOUtils.readString(data));
    }
}
