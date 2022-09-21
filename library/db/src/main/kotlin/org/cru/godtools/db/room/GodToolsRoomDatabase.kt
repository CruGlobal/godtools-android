package org.cru.godtools.db.room

import androidx.room.Database
import androidx.room.RoomDatabase
import org.cru.godtools.db.room.dao.UserCountersDao
import org.cru.godtools.db.room.entity.UserCounterEntity
import org.cru.godtools.db.room.repository.UserCountersRoomRepository

@Database(
    entities = [UserCounterEntity::class],
    version = 1
)
internal abstract class GodToolsRoomDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "GodTools"
    }

    // region DAOs
    abstract val userCountersDao: UserCountersDao
    // endregion DAOs

    // region Repositories
    abstract val userCountersRepository: UserCountersRoomRepository
    // endregion Repositories
}
