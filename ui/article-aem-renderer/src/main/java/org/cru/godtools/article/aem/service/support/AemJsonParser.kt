package org.cru.godtools.article.aem.service.support

import android.net.Uri
import org.cru.godtools.article.aem.model.Article
import org.json.JSONObject

private const val TAG_TYPE = "jcr:primaryType"
private const val TAG_TEMPLATE = "cq:template"
private const val TAG_UUID = "jcr:uuid"
private const val TAG_TITLE = "jcr:title"
private const val TAG_CONTENT = "jcr:content"
private const val TAG_TAGS = "cq:tags"
private const val TAG_CANONICAL_URL = "xfCanonical"

private const val TYPE_FOLDER = "sling:Folder"
private const val TYPE_ORDERED_FOLDER = "sling:OrderedFolder"
private const val TYPE_PAGE = "cq:Page"

private val SUPPORTED_TEMPLATES = setOf(
    // GodTools template
    "/conf/cru/settings/wcm/templates/experience-fragment-cru-godtools-variation"
)

/**
 * Find and parse any AEM articles in the provided JSONObject
 */
fun JSONObject.findAemArticles(url: Uri): Sequence<Article> {
    // parse this JSON node based on it's type & subtype
    return when (optString(TAG_TYPE)) {
        TYPE_PAGE -> {
            if (SUPPORTED_TEMPLATES.contains(optJSONObject(TAG_CONTENT)?.optString(TAG_TEMPLATE))) {
                sequenceOf(parseAemArticle(url))
            } else {
                parseNestedAemObject(url)
            }
        }
        TYPE_ORDERED_FOLDER, TYPE_FOLDER -> parseNestedAemObject(url)
        else -> emptySequence()
    }
}

/**
 * Recursively parse any nested AEM objects for articles.
 */
private fun JSONObject.parseNestedAemObject(url: Uri): Sequence<Article> {
    return keys().asSequence()
        .filterNot { isAemMetadataKey(it) }
        .flatMap { optJSONObject(it)?.findAemArticles(url.buildUpon().appendPath(it).build()) ?: emptySequence() }
}

/**
 * Parse an AEM JSONObject into an Article
 */
private fun JSONObject.parseAemArticle(url: Uri): Article {
    return Article(url).also { article ->
        optJSONObject(TAG_CONTENT)?.apply {
            article.uuid = optString(TAG_UUID, article.uuid)
            article.canonicalUri = optString(TAG_CANONICAL_URL).takeUnless { it.isBlank() }?.let { Uri.parse(it) }
            article.title = optString(TAG_TITLE, article.title)
            article.tags = optJSONArray(TAG_TAGS)?.run {
                IntRange(0, length() - 1).asSequence()
                    .map { optString(it, null) }
                    .filterNotNull()
                    .toList()
            } ?: emptyList()
        }
    }
}

/**
 * This method will make an educated decision on if the key is a metadata key.
 */
private fun isAemMetadataKey(key: String): Boolean {
    return key.startsWith("jcr:") ||
            key.startsWith("cq:") ||
            key.startsWith("sling:")
}
