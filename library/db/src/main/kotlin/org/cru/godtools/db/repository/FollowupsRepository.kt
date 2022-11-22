package org.cru.godtools.db.repository

import org.cru.godtools.model.Followup

interface FollowupsRepository {
    suspend fun createFollowup(followup: Followup)
}
