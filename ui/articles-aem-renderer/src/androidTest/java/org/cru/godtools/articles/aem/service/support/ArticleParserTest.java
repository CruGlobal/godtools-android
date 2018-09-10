package org.cru.godtools.articles.aem.service.support;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import org.ccci.gto.android.common.util.IOUtils;
import org.cru.godtools.articles.aem.model.Article;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ArticleParserTest {
    @Test
    public void verifyArticleParseLogic() throws Exception {
            final JSONObject jsonObject = loadJson("tests/article-test.json");

            HashMap<String, Object> results = ArticleParser.execute(jsonObject);

            assertTrue(results.containsKey(ArticleParser.ARTICLE_LIST_KEY));

            List<Article> resultsArticles = (List<Article>) results.get(ArticleParser.ARTICLE_LIST_KEY);
            assertThat(resultsArticles.size(), is(2));
        for (final Article article : resultsArticles) {
            assertThat(article.parsedAttachments.size(), is(3));
        }
    }

    private JSONObject loadJson(@NonNull final String file) throws Exception {
        final InputStream data = getInstrumentation().getContext().getAssets().open(file);
        return new JSONObject(IOUtils.readString(data));
    }
}
