package org.cru.godtools.download.manager.db

import dagger.Reusable
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.db.Expression.Companion.bind
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.model.Translation
import org.keynote.godtools.android.db.Contract
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

private val QUERY_FAVORITE_TRANSLATIONS = Query.select<Translation>()
    .joins(TranslationTable.SQL_JOIN_TOOL)
    .where(
        Contract.ToolTable.FIELD_ADDED.eq(true)
            .and(TranslationTable.SQL_WHERE_PUBLISHED)
            .and(TranslationTable.FIELD_DOWNLOADED.eq(false))
    )

@Reusable
internal class DownloadManagerRepository @Inject constructor(private val dao: GodToolsDao) {
    fun getFavoriteTranslationsThatNeedDownload(languages: Collection<Locale>) =
        QUERY_FAVORITE_TRANSLATIONS
            .andWhere(TranslationTable.FIELD_LANGUAGE.oneOf(languages.map { bind(it) }))
            .getAsFlow(dao)
}
