package org.cru.godtools.article.aem.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import org.ccci.gto.android.common.base.TimeConstants.DAY_IN_MS

private const val STALE_AGE = DAY_IN_MS

@Entity(tableName = "aemImports")
class AemImport(@field:PrimaryKey val uri: Uri) {
    var lastProcessed = Date(0)
    var lastAccessed = Date(0)

    fun isStale(): Boolean {
        return lastProcessed.before(Date(System.currentTimeMillis() - STALE_AGE))
    }

    @Entity(tableName = "aemImportArticles",
            primaryKeys = ["aemImportUri", "articleUri"],
            indices = [Index("articleUri")],
            foreignKeys = [
                ForeignKey(entity = AemImport::class,
                        onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE,
                        parentColumns = ["uri"], childColumns = ["aemImportUri"]),
                ForeignKey(entity = Article::class,
                        onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE,
                        parentColumns = ["uri"], childColumns = ["articleUri"])])
    class AemImportArticle(val aemImportUri: Uri, val articleUri: Uri) {
        constructor(aemImport: AemImport, article: Article) : this(aemImport.uri, article.uri)
    }
}
