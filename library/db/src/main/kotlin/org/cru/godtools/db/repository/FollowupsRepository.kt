package org.cru.godtools.db.repository

import org.cru.godtools.model.Followup

interface FollowupsRepository {
    suspend fun createFollowup(followup: Followup)
    suspend fun getFollowups(): List<Followup>
    suspend fun deleteFollowup(followup: Followup)
}
