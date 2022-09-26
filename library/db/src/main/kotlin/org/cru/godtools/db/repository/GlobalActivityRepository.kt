package org.cru.godtools.db.repository

import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.GlobalActivityAnalytics

interface GlobalActivityRepository {
    fun getGlobalActivityFlow(): Flow<GlobalActivityAnalytics>
    suspend fun updateGlobalActivity(activity: GlobalActivityAnalytics)
}
