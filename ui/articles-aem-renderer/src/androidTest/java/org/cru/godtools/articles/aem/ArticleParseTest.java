package org.cru.godtools.articles.aem;

import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;

import org.apache.commons.io.IOUtils;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.cru.godtools.articles.aem.service.support.ArticleParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ArticleParseTest {

    @Test
    public void verifyArticleParseLogic() {

        try {
            Uri url = Uri.parse("https://stage.cru.org/content/experience-fragments/questions_about_god/english.999.json");

            JSONObject jsonObject = new JSONObject(IOUtils.toString(
                    new URI(url.toString()))
            );

            HashMap<String, Object> results = new ArticleParser(jsonObject).execute();

            assertTrue(results.containsKey(ArticleParser.ATTACHMENT_LIST_KEY));
            assertTrue(results.containsKey(ArticleParser.ARTICLE_LIST_KEY));

            List<Article> resultsArticles = (List<Article>) results.get(ArticleParser.ARTICLE_LIST_KEY);
            assertTrue(resultsArticles.size() > 0);
            List<Attachment> resultAttachments = (List<Attachment>) results.get(ArticleParser.ATTACHMENT_LIST_KEY);
            assertTrue(resultAttachments.size() > 0);
        } catch (MalformedURLException | JSONException | URISyntaxException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }

    }
}
