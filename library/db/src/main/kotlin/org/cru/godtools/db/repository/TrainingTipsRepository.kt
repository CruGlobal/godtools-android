package org.cru.godtools.db.repository

import java.util.Locale
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.TrainingTip

interface TrainingTipsRepository {
    fun getCompletedTipsFlow(): Flow<Set<TrainingTip>>

    suspend fun markTipComplete(tool: String, locale: Locale, tipId: String)
    fun isTipCompleteFlow(tool: String, locale: Locale, tipId: String): Flow<Boolean>
}
