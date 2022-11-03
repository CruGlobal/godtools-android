package org.cru.godtools.db.repository

import java.util.Locale
import kotlinx.coroutines.flow.Flow

interface TrainingTipsRepository {
    fun isTipCompleteFlow(tool: String, locale: Locale, tipId: String): Flow<Boolean>
}
