package org.cru.godtools.articles.aem.service.support;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.cru.godtools.articles.aem.model.Article;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * This class handles parsing any AEM json calls into DOA objects.
 */
public class AemJsonParser {
    private static final String TAG_TYPE = "jcr:primaryType";
    private static final String TAG_SUBTYPE_RESOURCE = "sling:resourceType";
    private static final String TAG_UUID = "jcr:uuid";
    private static final String TAG_TITLE = "jcr:title";
    private static final String TAG_CONTENT = "jcr:content";
    private static final String TAG_TAGS = "cq:tags";

    private static final String TYPE_FOLDER = "sling:Folder";
    private static final String TYPE_ORDERED_FOLDER = "sling:OrderedFolder";
    private static final String TYPE_PAGE = "cq:Page";
    private static final String SUBTYPE_XFPAGE = "cq/experience-fragments/components/xfpage";

    private static final String CREATED_TAG = "jcr:created";
    private static final String LAST_MODIFIED_TAG = "cq:lastModified";

    // region Article Parsing

    /**
     * This executes the parsing of the local JsonObject.
     *
     * @return return a list of {@link Article}
     */
    public static Stream<Article> findArticles(@NonNull final Uri url, @NonNull final JSONObject json) {
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
            case TYPE_FOLDER:
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
    private static Stream<Article> parseNestedObject(@NonNull final Uri url, @NonNull final JSONObject json) {
        return Stream.of(json.keys())
                .filterNot(AemJsonParser::isMetadataKey)
                .flatMap(k -> {
                    final JSONObject child = json.optJSONObject(k);
                    return child != null ? findArticles(url.buildUpon().appendPath(k).build(), child) : null;
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
        final JSONObject content = json.optJSONObject(TAG_CONTENT);

        // Create Article
        final Article article = new Article(url);
        article.mDateCreated = getDateLongFromJsonString(json.optString(CREATED_TAG));
        if (content != null) {
            article.uuid = content.optString(TAG_UUID, article.uuid);
            article.title = content.optString(TAG_TITLE, article.title);

            // store any tags
            final JSONArray tagsJson = content.optJSONArray(TAG_TAGS);
            final List<String> tags = new ArrayList<>();
            for (int i = 0; i < tagsJson.length(); i++) {
                final String tag = tagsJson.optString(i, null);
                if (tag == null) {
                    continue;
                }
                tags.add(tag);
            }
            article.setTags(tags);

            if (content.has(LAST_MODIFIED_TAG)) {
                article.mDateUpdated = getDateLongFromJsonString(content.optString(LAST_MODIFIED_TAG));
            }
        }

        return article;
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
