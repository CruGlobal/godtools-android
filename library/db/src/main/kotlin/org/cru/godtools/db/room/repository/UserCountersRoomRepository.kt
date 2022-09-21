package org.cru.godtools.db.room.repository

import androidx.room.Dao
import androidx.room.Transaction
import org.cru.godtools.db.repository.UserCountersRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.entity.UserCounterEntity

@Dao
internal abstract class UserCountersRoomRepository(private val db: GodToolsRoomDatabase) : UserCountersRepository {
    private val dao get() = db.userCountersDao

    @Transaction
    override suspend fun updateCounter(name: String, delta: Int) {
        dao.insertOrIgnore(UserCounterEntity(name))
        if (delta != 0) dao.updateUserCounterDelta(name, delta)
    }
}
