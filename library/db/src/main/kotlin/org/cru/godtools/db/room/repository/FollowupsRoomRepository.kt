package org.cru.godtools.db.room.repository

import androidx.room.Dao
import org.cru.godtools.db.repository.FollowupsRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.entity.FollowupEntity
import org.cru.godtools.model.Followup

@Dao
internal abstract class FollowupsRoomRepository(private val db: GodToolsRoomDatabase) : FollowupsRepository {
    val dao get() = db.followupsDao

    override suspend fun createFollowup(followup: Followup) {
        val entity = FollowupEntity(followup)
        entity.id = null
        dao.insert(entity)
    }

    override suspend fun getFollowups() = dao.getFollowups().map { it.toModel() }
    override suspend fun deleteFollowup(followup: Followup) = dao.delete(FollowupEntity(followup))
}
