package org.keynote.godtools.android.db.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.db.Query
import org.cru.godtools.db.repository.FollowupsRepository
import org.cru.godtools.model.Followup
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
internal class LegacyFollowupsRepository @Inject constructor(private val dao: GodToolsDao) : FollowupsRepository {
    override suspend fun createFollowup(followup: Followup): Unit = withContext(dao.coroutineDispatcher) {
        dao.insertNew(followup)
    }

    override suspend fun getFollowups() = dao.getAsync(Query.select<Followup>()).await()

    override suspend fun deleteFollowup(followup: Followup) = withContext(dao.coroutineDispatcher) {
        dao.delete(followup)
    }
}
