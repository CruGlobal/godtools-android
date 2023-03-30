package org.cru.godtools.db.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.ccci.gto.android.common.androidx.room.converter.Java8TimeConverters
import org.ccci.gto.android.common.androidx.room.converter.LocaleConverter
import org.cru.godtools.db.room.dao.FollowupsDao
import org.cru.godtools.db.room.dao.GlobalActivityDao
import org.cru.godtools.db.room.dao.LanguagesDao
import org.cru.godtools.db.room.dao.LastSyncTimeDao
import org.cru.godtools.db.room.dao.TrainingTipDao
import org.cru.godtools.db.room.dao.UserCountersDao
import org.cru.godtools.db.room.dao.UserDao
import org.cru.godtools.db.room.entity.FollowupEntity
import org.cru.godtools.db.room.entity.GlobalActivityEntity
import org.cru.godtools.db.room.entity.LanguageEntity
import org.cru.godtools.db.room.entity.LastSyncTimeEntity
import org.cru.godtools.db.room.entity.TrainingTipEntity
import org.cru.godtools.db.room.entity.UserCounterEntity
import org.cru.godtools.db.room.entity.UserEntity
import org.cru.godtools.db.room.repository.FollowupsRoomRepository
import org.cru.godtools.db.room.repository.GlobalActivityRoomRepository
import org.cru.godtools.db.room.repository.LanguagesRoomRepository
import org.cru.godtools.db.room.repository.LastSyncTimeRoomRepository
import org.cru.godtools.db.room.repository.TrainingTipsRoomRepository
import org.cru.godtools.db.room.repository.UserCountersRoomRepository
import org.cru.godtools.db.room.repository.UserRoomRepository

@Database(
    version = 7,
    entities = [
        LanguageEntity::class,
        FollowupEntity::class,
        GlobalActivityEntity::class,
        TrainingTipEntity::class,
        UserEntity::class,
        UserCounterEntity::class,
        LastSyncTimeEntity::class,
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
    ],
)
@TypeConverters(Java8TimeConverters::class, LocaleConverter::class)
internal abstract class GodToolsRoomDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "GodTools"
    }

    // region DAOs
    abstract val followupsDao: FollowupsDao
    abstract val globalActivityDao: GlobalActivityDao
    abstract val languagesDao: LanguagesDao
    abstract val trainingTipDao: TrainingTipDao
    abstract val userDao: UserDao
    abstract val userCountersDao: UserCountersDao
    abstract val lastSyncTimeDao: LastSyncTimeDao
    // endregion DAOs

    // region Repositories
    abstract val followupsRepository: FollowupsRoomRepository
    abstract val globalActivityRepository: GlobalActivityRoomRepository
    abstract val languagesRepository: LanguagesRoomRepository
    abstract val trainingTipsRepository: TrainingTipsRoomRepository
    abstract val userRepository: UserRoomRepository
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
 * 3: 2022-10-12
 * 4: 2022-11-03
 * 5: 2022-11-22
 * 6: 2022-12-05
 * v6.1.0-v6.1.1
 * 7: 2023-03-27
 */

internal fun RoomDatabase.Builder<GodToolsRoomDatabase>.enableMigrations() = fallbackToDestructiveMigration()
// endregion Migrations
