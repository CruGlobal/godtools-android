package org.cru.godtools.db.repository

import java.util.Locale
import kotlinx.coroutines.flow.Flow

interface TrainingTipsRepository {
    suspend fun markTipComplete(tool: String, locale: Locale, tipId: String)
    fun isTipCompleteFlow(tool: String, locale: Locale, tipId: String): Flow<Boolean>
}
