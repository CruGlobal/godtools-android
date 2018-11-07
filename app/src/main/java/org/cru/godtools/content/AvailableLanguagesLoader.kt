package org.cru.godtools.content

import android.content.Context

import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader
import org.cru.godtools.model.Translation
import org.cru.godtools.model.loader.TranslationEventBusSubscriber
import org.cru.godtools.util.asSequence
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao
import java.util.Locale

class AvailableLanguagesLoader(context: Context, private val tool: String) :
    CachingAsyncTaskEventBusLoader<List<@JvmSuppressWildcards Locale>>(context) {
    private val dao = GodToolsDao.getInstance(context)

    init {
        addEventBusSubscriber(TranslationEventBusSubscriber(this))
    }

    override fun loadInBackground(): List<Locale>? {
        return dao.streamCompat(Query.select(Translation::class.java).where(TranslationTable.FIELD_TOOL.eq(tool)))
            .asSequence()
            .map { it.languageCode }
            .distinct()
            .toList()
    }
}
