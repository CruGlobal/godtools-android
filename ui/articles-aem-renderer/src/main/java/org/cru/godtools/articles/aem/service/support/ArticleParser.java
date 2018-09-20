package org.cru.godtools.articles.aem.service.support;

import android.support.annotation.NonNull;

import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * This class handles parsing any AEM json calls into DOA objects.
 */
public class ArticleParser {
    private static final String CREATED_TAG = "jcr:created";
    private static final String CONTENT_TAG = "jcr:content";
    private static final String LAST_MODIFIED_TAG = "cq:lastModified";
    private static final String UUID_TAG = "jcr:uuid";
    private static final String TITLE_TAG = "jcr:title";
    private static final String ROOT_TAG = "root";
    private static final String FILE_TAG = "fileReference";
    private static final String BASE_URL = "https://stage.cru.org";
    private static final String RESOURCE_TYPE_TAG = "sling:resourceType";
    private static final String PRIMARY_TYPE_TAG = "jcr:primaryType";
    private static final String ORDER_FOLDER_TAG = "sling:OrderedFolder";
    private static final String PAGE_TAG = "cq:Page";

    //region Public Executor
    /**
     * This executes the parsing of the local JsonObject.
     *
     * @return return a list of {@link Article}
     */
    public static List<Article> parse(@NonNull final JSONObject articleJSON, String url) {
        String baseURL = getArticleBaseUrl(url);
        return jsonObjectHandler(articleJSON, new ArrayList<>(), baseURL);
    }
    //endregion public Executor

    //region Article Parsing
    /**
     * This method takes in a Json Object and Iterates through the keys to determine if they
     * are Article Objects or Category Object.  Article Objects will be passed on to be parsed
     * and Category Object will be Recursively placed back into this method.
     *
     * @param evaluatingObject the Json Object to be evaluated.
     * @param urlParams
     * @param baseURL
     * @return All the articles that were parsed
     */
    @NonNull
    private static List<Article> jsonObjectHandler(JSONObject evaluatingObject,
                                                   List<String> urlParams, String baseURL) {
        final List<Article> articles = new ArrayList<>();

        // Create loop through Keys
        Iterator<String> keys = evaluatingObject.keys();
        while (keys.hasNext()) {
            String nextKey = keys.next();
            if (isArticleOrCategory(nextKey)) {
                JSONObject returnedObject = evaluatingObject.optJSONObject(nextKey);
                if (returnedObject != null) {
                    urlParams.add(nextKey);
                    switch (returnedObject.optString(PRIMARY_TYPE_TAG)) {
                        case ORDER_FOLDER_TAG:
                            articles.addAll(jsonObjectHandler(returnedObject, urlParams, baseURL));
                            // This param has been used and needs to be removed before looping.

                            break;
                        case PAGE_TAG:
                            articles.add(parseArticleObject(returnedObject, urlParams, baseURL));
                            // This param has been used and needs to be removed before looping.

                            break;
                    }
                    urlParams = new ArrayList<>();
                }
            }
        }

        return articles;
    }

    /**
     * This method parses an Article Json Object into the Database. On completion it will add
     * articles to <code>articleList</code>
     *
     * @param articleObject article JsonObject
     * @param urlParams
     * @param baseURL
     * @return The AEM Article that was just parsed
     */
    @NonNull
    private static Article parseArticleObject(JSONObject articleObject, List<String> urlParams, String baseURL) {
        // Create Article
        Article retrievedArticle = new Article();
        // get Inner article Object
        Iterator<String> keys = articleObject.keys();
        JSONObject articleTagObject = null;
        while (keys.hasNext()) {
            String nextKey = keys.next();
            if (isArticleOrCategory(nextKey)) {
                urlParams.add(nextKey);
                articleTagObject = articleObject.optJSONObject(nextKey);
            }
        }

        retrievedArticle.mDateCreated = getDateLongFromJsonString(articleObject.optString(CREATED_TAG));
        JSONObject contentObject = articleTagObject != null ? articleTagObject.optJSONObject(CONTENT_TAG) : null;
        if (contentObject != null) {
            if (articleObject.has(CONTENT_TAG) && contentObject.has(LAST_MODIFIED_TAG)) {
                retrievedArticle.mDateUpdated = getDateLongFromJsonString(contentObject
                        .optString(LAST_MODIFIED_TAG));
            }
            StringBuilder urlKey = new StringBuilder(baseURL);
            for (int i = 0; i < urlParams.size(); i++) {
                urlKey.append("/").append(urlParams.get(i));
            }

            urlKey.append(".html");
            retrievedArticle.mkey = urlKey.toString();
            retrievedArticle.mTitle = contentObject
                    .optString(TITLE_TAG);

            JSONObject articleRootObject = contentObject.optJSONObject(ROOT_TAG);

            // get Attachments from Articles
            if (articleRootObject != null) {
                retrievedArticle.parsedAttachments =
                        getAttachmentsFromRootObject(articleRootObject, retrievedArticle.mkey);
            }
        }

        return retrievedArticle;
    }

    /**
     * This method if for extracting Attachments from the root Json Object of the article.  On
     * completion it will add Attachment to <code>attachmentList</code>
     *
     * @param articleRootObject the root json Object of Article
     * @param articleKey        the uuid of the article
     * @return the list of attachments that were parsed
     */
    @NonNull
    private static List<Attachment> getAttachmentsFromRootObject(JSONObject articleRootObject, String articleKey) {
        final List<Attachment> attachments = new ArrayList<>();
        String rootUrl;
        try {
            rootUrl = new URL(articleKey).getHost();
        } catch (MalformedURLException e) {
            Timber.tag("ArticleParser").e(e);
            rootUrl = BASE_URL;
        }
        // Iterate through keys
        Iterator<String> keys = articleRootObject.keys();
        while (keys.hasNext()) {
            String nextKey = keys.next();
            JSONObject innerObject = articleRootObject.optJSONObject(nextKey);
            if (innerObject != null && innerObject.has(RESOURCE_TYPE_TAG) &&
                    "wcm/foundation/components/image".equals(innerObject
                            .optString(RESOURCE_TYPE_TAG))) {

                //  This Key is an Attachment
                Attachment retrievedAttachment = new Attachment();
                retrievedAttachment.mArticleKey = articleKey;
                retrievedAttachment.mAttachmentUrl = String.format("https://%s%s", rootUrl,
                        innerObject.optString(FILE_TAG));
                attachments.add(retrievedAttachment);
            }
        }

        return attachments;
    }

    /**
     * This method is used to convert a json Date string to long.
     *
     * @param dateString the string representation of Date
     * @return Date as a long
     */
    private static long getDateLongFromJsonString(String dateString) {
        try {
            return new SimpleDateFormat("E MMM dd yyyy HH:mm:ss zz",
                    Locale.getDefault()).parse(dateString).getTime();
        } catch (ParseException e) {
            return new Date().getTime();
        }
    }

    /**
     * Using "https://stage.cru.org/content/experience-fragments/questions_about_god/english.9999.json"
     * as an example url this method will remove ".999.json" from the url leaving the base url.
     * @param url
     * @return
     */
    private static String getArticleBaseUrl(String url) {
        url = url.substring(0, url.lastIndexOf("."));
        return url.substring(0, url.lastIndexOf("."));
    }
    //endregion Article Parsing

    //region Validation
    /**
     * This method will determine if the key is an Article or Category
     *
     * @param key Json Key
     * @return true if the key is associated with an Article or Category
     */
    private static Boolean isArticleOrCategory(String key) {
        return !key.startsWith("jcr:") &&
                !key.startsWith("cq:") &&
                !key.startsWith("sling:");
    }
    // endregion Validation
}
