package org.cru.godtools.article.aem.model

import android.net.Uri
import androidx.annotation.RestrictTo
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.common.collect.ImmutableList
import javax.annotation.concurrent.Immutable

@Entity(tableName = "articles")
class Article(@field:PrimaryKey val uri: Uri) {
    var uuid = ""
    var title = ""
    var contentUuid: String? = null
    var content: String? = null

    var canonicalUri: Uri? = null
    var shareUri: Uri? = null

    @Ignore
    var tags: List<String> = ImmutableList.of()
        set(it) {
            field = ImmutableList.copyOf(it)
        }

    @Ignore
    var resources: List<Resource> = ImmutableList.of()

    val tagObjects: List<Tag>
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        get() = tags.map { Tag(this, it) }

    @Immutable
    @Entity(tableName = "articleTags",
            primaryKeys = ["articleUri", "tag"],
            foreignKeys = [
                ForeignKey(entity = Article::class,
                        onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE,
                        parentColumns = ["uri"], childColumns = ["articleUri"])])
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    data class Tag(val articleUri: Uri, val tag: String) {
        constructor(article: Article, tag: String) : this(article.uri, tag)
    }

    @Immutable
    @Entity(tableName = "articleResources",
            primaryKeys = ["articleUri", "resourceUri"],
            indices = [Index("resourceUri")],
            foreignKeys = [
                ForeignKey(entity = Article::class,
                        onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE,
                        parentColumns = ["uri"], childColumns = ["articleUri"]),
                ForeignKey(entity = Resource::class,
                        onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE,
                        parentColumns = ["uri"], childColumns = ["resourceUri"])])
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
}
