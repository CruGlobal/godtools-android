package org.cru.godtools.db.room.repository

import androidx.room.Dao
import kotlinx.coroutines.flow.map
import org.cru.godtools.db.repository.GlobalActivityRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.entity.GlobalActivityEntity
import org.cru.godtools.model.GlobalActivityAnalytics

@Dao
internal abstract class GlobalActivityRoomRepository(private val db: GodToolsRoomDatabase) : GlobalActivityRepository {
    val dao get() = db.globalActivityDao

    override fun getGlobalActivityFlow() = dao.findGlobalActivityFlow()
        .map { it?.toModel() ?: GlobalActivityAnalytics() }

    override suspend fun updateGlobalActivity(activity: GlobalActivityAnalytics) {
        dao.insertOrReplace(GlobalActivityEntity(activity))
    }
}
