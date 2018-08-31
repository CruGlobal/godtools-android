package org.cru.godtools.articles.aem.service.support;

import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ArticleParser {

    public static final String ARTICLE_LIST_KEY = "article_list_key";

    public static final String ATTACHMENT_LIST_KEY = "attachment_list_key";

    private JSONObject articleJSON;


    private List<Attachment> attachmentList = new ArrayList<>();

    private List<Article> articleList = new ArrayList<>();

    private String CREATED_TAG = "jcr:created";
    private String CONTENT_TAG = "jcr:content";
    private String LAST_MODIFIED_TAG = "cq:lastModified";
    private String UUID_TAG = "jcr:uuid";
    private String TITLE_TAG = "jcr:title";
    private String ROOT_TAG = "root";
    private String FILE_TAG = "fileReference";
    private String BASE_URL = "https://stage.cru.org";

    public ArticleParser(JSONObject jsonObject) {
        this.articleJSON = jsonObject;
    }

    public HashMap<String, Object> execute(JSONObject jsonObject) {
        this.articleJSON = jsonObject;
        return execute();
    }

    public HashMap<String, Object> execute() {

        // Create loop through Keys
        Iterator<String> keys = articleJSON.keys();
        while (keys.hasNext()) {
            String nextKey = keys.next();
            if (isArticleOrCategory(nextKey)) {
                try {
                    JSONObject returnedObject = articleJSON.getJSONObject(nextKey);
                    if (isObjectCategory(returnedObject)) {
                        getArticleFromCategory(returnedObject);
                    } else {
                        // Get inner Article Object
                        parseArticleObject(returnedObject, nextKey);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        HashMap<String, Object> returnObject = new HashMap<>();
        returnObject.put(ARTICLE_LIST_KEY, articleList);
        returnObject.put(ATTACHMENT_LIST_KEY, attachmentList);

        return returnObject;
    }

    private void getArticleFromCategory(JSONObject categoryObject) {
        Iterator<String> keys = categoryObject.keys();
        while (keys.hasNext()) {
            String nextKey = keys.next();
            if (isArticleOrCategory(nextKey)) {
                try {
                    JSONObject articleObject = categoryObject.getJSONObject(nextKey);
                    parseArticleObject(articleObject, nextKey);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void parseArticleObject(JSONObject articleObject, String articleTag) throws JSONException, ParseException {
        // Create Article
        Article retrievedArticle = new Article();
        retrievedArticle.mDateCreated = getDateLongFromJsonString(articleObject.getString(CREATED_TAG));
        if (articleObject.has(CONTENT_TAG) && articleObject.getJSONObject(CONTENT_TAG)
                .has(LAST_MODIFIED_TAG)) {
            retrievedArticle.mDateUpdated = getDateLongFromJsonString(articleObject
                    .getJSONObject(CONTENT_TAG).getString(LAST_MODIFIED_TAG));
        }
        retrievedArticle.mkey = articleObject.getJSONObject(CONTENT_TAG).getString(UUID_TAG);
        retrievedArticle.mTitle = articleObject.getJSONObject(articleTag).getJSONObject(CONTENT_TAG)
                .getString(TITLE_TAG);

        JSONObject articleRootObject = articleObject.getJSONObject(articleTag)
                .getJSONObject(CONTENT_TAG).getJSONObject(ROOT_TAG);

        retrievedArticle.mContent = articleRootObject.getJSONObject("text").getString("text");

        // add retrieved Article to List
        articleList.add(retrievedArticle);

        // get Attachments from Articles
        getAttachmentsFromRootObject(articleRootObject, retrievedArticle.mkey);


    }

    private void getAttachmentsFromRootObject(JSONObject articleRootObject, String articlKey) {
        // Iterate through keys
        Iterator<String> keys = articleRootObject.keys();
        while (keys.hasNext()) {
            String nextKey = keys.next();
            if (nextKey.contains("image")) {
                //  This Key is an Attachment
                try {
                    Attachment retrievedAttachment = new Attachment();
                    retrievedAttachment.mArticleKey = articlKey;
                    retrievedAttachment.mAttachmentUrl = String.format("%s%s", BASE_URL,
                            articleRootObject.getJSONObject(nextKey).getString(FILE_TAG));
                    attachmentList.add(retrievedAttachment);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private long getDateLongFromJsonString(String dateString) throws ParseException {
        return new SimpleDateFormat("E MMM dd yyyy HH:mm:ss zz",
                Locale.getDefault()).parse(dateString).getTime();
    }

    //region Validation

    /**
     * This method will determine if the key is an Article or Category
     *
     * @param key = Json Key
     * @return = true if the key is associated with an Article or Category
     */
    private Boolean isArticleOrCategory(String key) {
        if (key.startsWith("jcr:") ||
                key.startsWith("cq:") ||
                key.startsWith("sling:")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * This method checks to see if jsonObject is a Category
     *
     * @param testObject = the jsonObject to test
     * @return = Boolean value on pass or fail
     * @throws Exception = Throws Exception on failure
     */
    private Boolean isObjectCategory(JSONObject testObject) throws Exception {
        if (testObject.has("jcr:primaryType")) {
            String type = testObject.getString("jcr:primaryType");
            return type.equals("sling:OrderedFolder");
        } else {
            throw new Exception("Object Doesn't contain primaryType Tag");
        }
    }
    // endregion Validation

}
