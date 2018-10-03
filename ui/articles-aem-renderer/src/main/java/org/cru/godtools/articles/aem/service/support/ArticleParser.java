package org.cru.godtools.articles.aem.service.support;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * This class handles parsing any AEM json calls into DOA objects.
 */
public class ArticleParser {
    private static final String TAG_TYPE = "jcr:primaryType";
    private static final String TAG_SUBTYPE_RESOURCE = "sling:resourceType";
    private static final String TAG_CONTENT = "jcr:content";

    private static final String TYPE_ORDERED_FOLDER = "sling:OrderedFolder";
    private static final String TYPE_PAGE = "cq:Page";
    private static final String SUBTYPE_XFPAGE = "cq/experience-fragments/components/xfpage";

    private static final String CREATED_TAG = "jcr:created";
    private static final String LAST_MODIFIED_TAG = "cq:lastModified";
    private static final String TITLE_TAG = "jcr:title";
    private static final String ROOT_TAG = "root";
    private static final String FILE_TAG = "fileReference";

    // region Article Parsing

    /**
     * This executes the parsing of the local JsonObject.
     *
     * @return return a list of {@link Article}
     */
    public static Stream<Article> parse(@NonNull final Uri url, @NonNull final JSONObject json) {
        // parse this JSON node based on it's type & subtype
        final String type = json.optString(TAG_TYPE, "");
        final String subtype = Optional.ofNullable(json.optJSONObject(TAG_CONTENT))
                .map(n -> n.optString(TAG_SUBTYPE_RESOURCE))
                .orElse("");
        switch (type) {
            case TYPE_PAGE:
                switch (subtype) {
                    case SUBTYPE_XFPAGE:
                        return Stream.of(parseArticle(url, json));
                }
            case TYPE_ORDERED_FOLDER:
                return parseNestedObject(url, json);
        }
        return Stream.of();
    }

    /**
     * Recursively parse any nested objects for articles.
     *
     * @param url  The url for the current json node being processed.
     * @param json The current JSON node
     * @return a Stream of all articles this json node contains
     */
    public static Stream<Article> parseNestedObject(@NonNull final Uri url, @NonNull final JSONObject json) {
        return Stream.of(json.keys())
                .filterNot(ArticleParser::isMetadataKey)
                .flatMap(k -> {
                    final JSONObject child = json.optJSONObject(k);
                    return child != null ? parse(url.buildUpon().appendPath(k).build(), child) : null;
                });
    }

    /**
     * This method parses an Article Json Object into the Database. On completion it will add
     * articles to <code>articleList</code>
     *
     * @param url The URL of this article
     * @param json article JsonObject
     * @return The AEM Article that was just parsed
     */
    @NonNull
    private static Article parseArticle(@NonNull final Uri url, @NonNull final JSONObject json) {
        // Create Article
        final Article retrievedArticle = new Article(url);
        retrievedArticle.mDateCreated = getDateLongFromJsonString(json.optString(CREATED_TAG));
        JSONObject contentObject = json.optJSONObject(TAG_CONTENT);
        if (contentObject != null) {
            if (json.has(TAG_CONTENT) && contentObject.has(LAST_MODIFIED_TAG)) {
                retrievedArticle.mDateUpdated = getDateLongFromJsonString(contentObject.optString(LAST_MODIFIED_TAG));
            }

            retrievedArticle.title = contentObject.optString(TITLE_TAG, retrievedArticle.title);

            JSONObject articleRootObject = contentObject.optJSONObject(ROOT_TAG);

            // get Attachments from Articles
            if (articleRootObject != null) {
                retrievedArticle.parsedAttachments = getAttachmentsFromRootObject(url, articleRootObject);
            }
        }

        return retrievedArticle;
    }

    /**
     * This method if for extracting Attachments from the root Json Object of the article.  On
     * completion it will add Attachment to <code>attachmentList</code>
     *
     * @param articleRootObject the root json Object of Article
     * @return the list of attachments that were parsed
     */
    @NonNull
    private static List<Attachment> getAttachmentsFromRootObject(@NonNull final Uri articleUrl,
                                                                 JSONObject articleRootObject) {
        final List<Attachment> attachments = new ArrayList<>();

        // Iterate through keys
        Iterator<String> keys = articleRootObject.keys();
        while (keys.hasNext()) {
            String nextKey = keys.next();
            JSONObject innerObject = articleRootObject.optJSONObject(nextKey);
            if (innerObject != null &&
                    "wcm/foundation/components/image".equals(innerObject.optString(TAG_SUBTYPE_RESOURCE))) {
                //  This Key is an Attachment
                final Uri attachmentUri = articleUrl.buildUpon().appendPath(nextKey).build();
                attachments.add(new Attachment(articleUrl, attachmentUri));
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

    // endregion Article Parsing

    //region Validation

    /**
     * This method will make an educated decision on if the key is a metadata key.
     *
     * @param key The key being examined
     * @return whether this is a metadata key or not
     */
    private static boolean isMetadataKey(@NonNull final String key) {
        return key.startsWith("jcr:") ||
                key.startsWith("cq:") ||
                key.startsWith("sling:");
    }

    // endregion Validation
}
