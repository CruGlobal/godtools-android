package org.cru.godtools.db.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import org.cru.godtools.db.room.dao.GlobalActivityDao
import org.cru.godtools.db.room.dao.LastSyncTimeDao
import org.cru.godtools.db.room.dao.UserCountersDao
import org.cru.godtools.db.room.entity.GlobalActivityEntity
import org.cru.godtools.db.room.entity.LastSyncTimeEntity
import org.cru.godtools.db.room.entity.UserCounterEntity
import org.cru.godtools.db.room.repository.GlobalActivityRoomRepository
import org.cru.godtools.db.room.repository.LastSyncTimeRoomRepository
import org.cru.godtools.db.room.repository.UserCountersRoomRepository

@Database(
    version = 2,
    entities = [GlobalActivityEntity::class, UserCounterEntity::class, LastSyncTimeEntity::class],
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
internal abstract class GodToolsRoomDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "GodTools"
    }

    // region DAOs
    abstract val userCountersDao: UserCountersDao
    abstract val lastSyncTimeDao: LastSyncTimeDao
    // endregion DAOs

    // region Repositories
    abstract val userCountersRepository: UserCountersRoomRepository
    abstract val lastSyncTimeRepository: LastSyncTimeRoomRepository
    // endregion Repositories
}

// region Migrations
/*
 * Version history
 *
 * v6.0.1
 * 1: 2022-09-22
 * 2: 2022-09-23
 */

internal fun RoomDatabase.Builder<GodToolsRoomDatabase>.enableMigrations() = fallbackToDestructiveMigration()
// endregion Migrations
