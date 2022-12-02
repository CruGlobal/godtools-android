package org.cru.godtools.db.repository

import java.util.Locale
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.Language

interface LanguagesRepository {
    fun getLanguageFlow(locale: Locale): Flow<Language?>
}
