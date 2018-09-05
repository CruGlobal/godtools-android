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

import timber.log.Timber;

/**
 * This class handles parsing any AEM json calls into DOA objects.
 */
public class ArticleParser {

    public static final String ARTICLE_LIST_KEY = "article_list_key";

    public static final String ATTACHMENT_LIST_KEY = "attachment_list_key";

    private JSONObject articleJSON;

    private List<Attachment> attachmentList = new ArrayList<>();

    private List<Article> articleList = new ArrayList<>();

    private static final String CREATED_TAG = "jcr:created";
    private static final String CONTENT_TAG = "jcr:content";
    private static final String LAST_MODIFIED_TAG = "cq:lastModified";
    private static final String UUID_TAG = "jcr:uuid";
    private static final String TITLE_TAG = "jcr:title";
    private static final String ROOT_TAG = "root";
    private static final String FILE_TAG = "fileReference";
    private static final String BASE_URL = "https://stage.cru.org";

    /**
     * Constructor
     * @param jsonObject = the Object to be parsed.
     */
    public ArticleParser(JSONObject jsonObject) {
        this.articleJSON = jsonObject;
    }

    //region Public Executor
    /**
     * This execute function allows you to change the initial jsonObject and run the execution.
     *
     * use <code>ARTICLE_LIST_KEY</code> and <code>ATTACHMENT_LIST_KEY</code> to access the Collections
     * out of the HashMaps
     *
     * @param jsonObject = the object to be parsed.
     * @return = return a list of <code>Article</code> and <code>Attachments</code> in HashMap
     */
    public HashMap<String, Object> execute(JSONObject jsonObject) {
        this.articleJSON = jsonObject;
        return execute();
    }

    /**
     * This executes the parsing of the local JsonObject.
     *
     * use <code>ARTICLE_LIST_KEY</code> and <code>ATTACHMENT_LIST_KEY</code> to access the Collections
     * out of the HashMaps
     *
     * @return = return a list of <code>Article</code> and <code>Attachments</code> in HashMap
     */
    public HashMap<String, Object> execute() {

        jsonObjectHandler(articleJSON);

        HashMap<String, Object> returnObject = new HashMap<>();
        returnObject.put(ARTICLE_LIST_KEY, articleList);
        returnObject.put(ATTACHMENT_LIST_KEY, attachmentList);

        return returnObject;
    }
    //endregion public Executor

    //region Article Parsing

    /**
     * This method takes in a Json Object and Iterates through the keys to determine if they
     * are Article Objects or Category Object.  Article Objects will be passed on to be parsed
     * and Category Object will be Recursively placed back into this method.
     * @param evaluatingObject = the Json Object to be evaluated.
     */
    private void jsonObjectHandler(JSONObject evaluatingObject) {
        // Create loop through Keys
        Iterator<String> keys = evaluatingObject.keys();
        while (keys.hasNext()) {
            String nextKey = keys.next();
            if (isArticleOrCategory(nextKey)) {
                try {
                    JSONObject returnedObject = evaluatingObject.getJSONObject(nextKey);
                    if (isObjectCategory(returnedObject)) {
                        jsonObjectHandler(returnedObject);
                    } else {
                        // Get inner Article Object
                        parseArticleObject(returnedObject);
                    }
                } catch (Exception e) {
                    Timber.e(e, "getArticleFromCategory: ");
                }
            }
        }
    }

    /**
     * This method parses an Article Json Object into the Database. On completion it will add
     * articles to <code>articleList</code>
     *
     * @param articleObject = article JsonObject
     * @throws Exception =
     */
    private void parseArticleObject(JSONObject articleObject) throws Exception {
        // Create Article
        Article retrievedArticle = new Article();
        // get Inner article Object
        Iterator<String> keys = articleObject.keys();
        JSONObject articleTagObject = null;
        while (keys.hasNext()) {
            String nextKey = keys.next();
            if (isArticleOrCategory(nextKey)) {
                articleTagObject = articleObject.getJSONObject(nextKey);
            }
        }

        if (articleTagObject == null) {
            throw new Exception("Article not configured properly");
        }
        retrievedArticle.mDateCreated = getDateLongFromJsonString(articleObject.getString(CREATED_TAG));
        if (articleObject.has(CONTENT_TAG) && articleObject.getJSONObject(CONTENT_TAG)
                .has(LAST_MODIFIED_TAG)) {
            retrievedArticle.mDateUpdated = getDateLongFromJsonString(articleObject
                    .getJSONObject(CONTENT_TAG).getString(LAST_MODIFIED_TAG));
        }
        retrievedArticle.mkey = articleObject.getJSONObject(CONTENT_TAG).getString(UUID_TAG);
        retrievedArticle.mTitle = articleTagObject.getJSONObject(CONTENT_TAG)
                .getString(TITLE_TAG);

        JSONObject articleRootObject = articleTagObject.getJSONObject(CONTENT_TAG)
                .getJSONObject(ROOT_TAG);

        retrievedArticle.mContent = articleRootObject.getJSONObject("text").getString("text");

        // add retrieved Article to List
        articleList.add(retrievedArticle);

        // get Attachments from Articles
        getAttachmentsFromRootObject(articleRootObject, retrievedArticle.mkey);


    }

    /**
     *  This method if for extracting Attachments from the root Json Object of the article.  On
     *  completion it will add Attachment to <code>attachmentList</code>
     *
     * @param articleRootObject = the root json Object of Article
     * @param articleKey = the uuid of the article
     */
    private void getAttachmentsFromRootObject(JSONObject articleRootObject, String articleKey) {
        // Iterate through keys
        Iterator<String> keys = articleRootObject.keys();
        while (keys.hasNext()) {
            String nextKey = keys.next();
            if (nextKey.contains("image")) {
                //  This Key is an Attachment
                try {
                    Attachment retrievedAttachment = new Attachment();
                    retrievedAttachment.mArticleKey = articleKey;
                    retrievedAttachment.mAttachmentUrl = String.format("%s%s", BASE_URL,
                            articleRootObject.getJSONObject(nextKey).getString(FILE_TAG));
                    attachmentList.add(retrievedAttachment);
                } catch (JSONException e) {
                    Timber.e(e, "getArticleFromCategory: ");
                }
            }
        }

    }

    /**
     * This method is used to convert a json Date string to long.
     *
     * @param dateString = the string representation of Date
     * @return = Date as a long
     * @throws ParseException
     */
    private long getDateLongFromJsonString(String dateString) throws ParseException {
        return new SimpleDateFormat("E MMM dd yyyy HH:mm:ss zz",
                Locale.getDefault()).parse(dateString).getTime();
    }
    //endregion Article Parsing

    //region Validation

    /**
     * This method will determine if the key is an Article or Category
     *
     * @param key = Json Key
     * @return = true if the key is associated with an Article or Category
     */
    private Boolean isArticleOrCategory(String key) {
        return !key.startsWith("jcr:") &&
                !key.startsWith("cq:") &&
                !key.startsWith("sling:");
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
            return "sling:OrderedFolder".equals(type);
        } else {
            throw new Exception("Object Doesn't contain primaryType Tag");
        }
    }
    // endregion Validation

}
