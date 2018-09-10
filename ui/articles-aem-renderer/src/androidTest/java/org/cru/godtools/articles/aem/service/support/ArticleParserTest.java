package org.cru.godtools.articles.aem.service.support;

import android.content.res.AssetManager;
import android.support.test.runner.AndroidJUnit4;

import org.ccci.gto.android.common.util.IOUtils;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ArticleParserTest {
    @Test
    public void verifyArticleParseLogic() throws IOException, JSONException {

            AssetManager manager = getInstrumentation().getContext().getAssets();

            InputStream input = manager.open("tests/article-test.json");

            String result = IOUtils.readString(input);

            JSONObject jsonObject = new JSONObject(result);

            HashMap<String, Object> results = ArticleParser.execute(jsonObject);

            assertTrue(results.containsKey(ArticleParser.ATTACHMENT_LIST_KEY));
            assertTrue(results.containsKey(ArticleParser.ARTICLE_LIST_KEY));

            List<Article> resultsArticles = (List<Article>) results.get(ArticleParser.ARTICLE_LIST_KEY);
            assertTrue(resultsArticles.size() > 0);
            List<Attachment> resultAttachments = (List<Attachment>) results.get(ArticleParser.ATTACHMENT_LIST_KEY);
            assertTrue(resultAttachments.size() > 0);
    }
}
