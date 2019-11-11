package org.cru.godtools.content

import android.content.Context
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader
import org.cru.godtools.model.Language
import org.cru.godtools.model.event.content.LanguageEventBusSubscriber
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

internal val QUERY_LANGUAGES = Query.select(Language::class.java)
    .join(LanguageTable.SQL_JOIN_TRANSLATION)
    .where(TranslationTable.SQL_WHERE_PUBLISHED)

class LanguagesLoader(context: Context) :
    CachingAsyncTaskEventBusLoader<List<@JvmSuppressWildcards Language>>(context) {
    private val dao = GodToolsDao.getInstance(context)

    init {
        addEventBusSubscriber(LanguageEventBusSubscriber(this))
    }

    override fun loadInBackground(): List<Language> = dao.get(QUERY_LANGUAGES)
}
