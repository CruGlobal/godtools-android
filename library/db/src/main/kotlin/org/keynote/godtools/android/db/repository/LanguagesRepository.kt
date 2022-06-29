package org.keynote.godtools.android.db.repository

import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import org.ccci.gto.android.common.androidx.collection.WeakLruCache
import org.ccci.gto.android.common.androidx.collection.getOrPut
import org.ccci.gto.android.common.db.findAsFlow
import org.cru.godtools.model.Language
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
class LanguagesRepository @Inject constructor(private val dao: GodToolsDao) {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    private val languagesCache = WeakLruCache<Locale, Flow<Language?>>(3)

    fun getLanguageFlow(locale: Locale) = languagesCache.getOrPut(locale) {
        dao.findAsFlow<Language>(it)
            .shareIn(coroutineScope, SharingStarted.WhileSubscribed(replayExpirationMillis = REPLAY_EXPIRATION), 1)
    }
}
