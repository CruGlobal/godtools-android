package org.cru.godtools.article.aem.model

import android.net.Uri
import androidx.annotation.RestrictTo
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import javax.annotation.concurrent.Immutable

@Entity(tableName = "articles")
class Article(@field:PrimaryKey val uri: Uri) {
    var uuid = ""
    var title = ""
    var contentUuid: String? = null
    var content: String? = null

    var canonicalUri: Uri? = null
    val shareUri
        get() = canonicalUri?.buildUpon()
            ?.appendQueryParameter("icid", "gtshare")
            ?.build()

    @Ignore
    var tags = emptyList<String>()
    internal val tagObjects get() = tags.map { Tag(this, it) }

    @Ignore
    var resources = emptyList<Resource>()

    @Immutable
    @Entity(
        tableName = "articleTags",
        primaryKeys = ["articleUri", "tag"],
        foreignKeys = [
            ForeignKey(
                entity = Article::class,
                parentColumns = ["uri"], childColumns = ["articleUri"],
                onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE
            )
        ]
    )
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    data class Tag(val articleUri: Uri, val tag: String) {
        constructor(article: Article, tag: String) : this(article.uri, tag)
    }

    @Immutable
    @Entity(
        tableName = "articleResources",
        primaryKeys = ["articleUri", "resourceUri"],
        indices = [Index("resourceUri")],
        foreignKeys = [
            ForeignKey(
                entity = Article::class,
                parentColumns = ["uri"], childColumns = ["articleUri"],
                onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                entity = Resource::class,
                parentColumns = ["uri"], childColumns = ["resourceUri"],
                onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE
            )
        ]
    )
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    data class ArticleResource(val articleUri: Uri, val resourceUri: Uri) {
        constructor(article: Article, resource: Resource) : this(article.uri, resource.uri)
    }

    // HACK: old database columns kept to simplify migrations
    @ColumnInfo(name = "date_created")
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    var deleteMe1: Int = 0
    @ColumnInfo(name = "date_updated")
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    var deleteMe2: Int = 0
    @ColumnInfo(name = "shareUri")
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    var deleteMe3: String? = null
}
