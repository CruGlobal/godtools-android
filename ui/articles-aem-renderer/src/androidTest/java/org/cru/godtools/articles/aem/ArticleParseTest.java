package org.cru.godtools.articles.aem;

import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;

import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.cru.godtools.articles.aem.service.support.ArticleParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ArticleParseTest {

    @Test
    public void verifyArticleParseLogic() {

        try {
            Uri url = Uri.parse("https://stage.cru.org/content/experience-fragments/questions_about_god/english.999.json");

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url.toString())
                    .build();

            Response response = client.newCall(request).execute();

            String result = response.body().string();

            JSONObject jsonObject = new JSONObject(result);

            HashMap<String, Object> results = new ArticleParser(jsonObject).execute();

            assertTrue(results.containsKey(ArticleParser.ATTACHMENT_LIST_KEY));
            assertTrue(results.containsKey(ArticleParser.ARTICLE_LIST_KEY));

            List<Article> resultsArticles = (List<Article>) results.get(ArticleParser.ARTICLE_LIST_KEY);
            assertTrue(resultsArticles.size() > 0);
            List<Attachment> resultAttachments = (List<Attachment>) results.get(ArticleParser.ATTACHMENT_LIST_KEY);
            assertTrue(resultAttachments.size() > 0);
        } catch (JSONException | IOException | IllegalStateException e) {
            fail(e.getMessage());
        }
    }
}
