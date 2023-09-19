package org.cru.godtools.db.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import org.ccci.gto.android.common.androidx.room.converter.Java8TimeConverters
import org.ccci.gto.android.common.androidx.room.converter.LocaleConverter
import org.cru.godtools.db.room.dao.AttachmentsDao
import org.cru.godtools.db.room.dao.DownloadedFilesDao
import org.cru.godtools.db.room.dao.FollowupsDao
import org.cru.godtools.db.room.dao.GlobalActivityDao
import org.cru.godtools.db.room.dao.LanguagesDao
import org.cru.godtools.db.room.dao.LastSyncTimeDao
import org.cru.godtools.db.room.dao.ToolsDao
import org.cru.godtools.db.room.dao.TrainingTipDao
import org.cru.godtools.db.room.dao.TranslationsDao
import org.cru.godtools.db.room.dao.UserCountersDao
import org.cru.godtools.db.room.dao.UserDao
import org.cru.godtools.db.room.entity.AttachmentEntity
import org.cru.godtools.db.room.entity.DownloadedFileEntity
import org.cru.godtools.db.room.entity.FollowupEntity
import org.cru.godtools.db.room.entity.GlobalActivityEntity
import org.cru.godtools.db.room.entity.LanguageEntity
import org.cru.godtools.db.room.entity.LastSyncTimeEntity
import org.cru.godtools.db.room.entity.ToolEntity
import org.cru.godtools.db.room.entity.TrainingTipEntity
import org.cru.godtools.db.room.entity.TranslationEntity
import org.cru.godtools.db.room.entity.UserCounterEntity
import org.cru.godtools.db.room.entity.UserEntity
import org.cru.godtools.db.room.repository.AttachmentsRoomRepository
import org.cru.godtools.db.room.repository.DownloadedFilesRoomRepository
import org.cru.godtools.db.room.repository.FollowupsRoomRepository
import org.cru.godtools.db.room.repository.GlobalActivityRoomRepository
import org.cru.godtools.db.room.repository.LanguagesRoomRepository
import org.cru.godtools.db.room.repository.LastSyncTimeRoomRepository
import org.cru.godtools.db.room.repository.ToolsRoomRepository
import org.cru.godtools.db.room.repository.TrainingTipsRoomRepository
import org.cru.godtools.db.room.repository.TranslationsRoomRepository
import org.cru.godtools.db.room.repository.UserCountersRoomRepository
import org.cru.godtools.db.room.repository.UserRoomRepository

@Database(
    version = 15,
    entities = [
        AttachmentEntity::class,
        LanguageEntity::class,
        DownloadedFileEntity::class,
        FollowupEntity::class,
        GlobalActivityEntity::class,
        ToolEntity::class,
        TrainingTipEntity::class,
        TranslationEntity::class,
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
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 12, to = 13),
        AutoMigration(from = 13, to = 14, spec = Migration14::class),
        AutoMigration(from = 14, to = 15),
    ],
)
@TypeConverters(Java8TimeConverters::class, LocaleConverter::class)
internal abstract class GodToolsRoomDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "GodTools"
    }

    // region DAOs
    abstract val attachmentsDao: AttachmentsDao
    abstract val downloadedFilesDao: DownloadedFilesDao
    abstract val followupsDao: FollowupsDao
    abstract val globalActivityDao: GlobalActivityDao
    abstract val languagesDao: LanguagesDao
    abstract val toolsDao: ToolsDao
    abstract val trainingTipDao: TrainingTipDao
    abstract val translationsDao: TranslationsDao
    abstract val userDao: UserDao
    abstract val userCountersDao: UserCountersDao
    abstract val lastSyncTimeDao: LastSyncTimeDao
    // endregion DAOs

    // region Repositories
    abstract val attachmentsRepository: AttachmentsRoomRepository
    abstract val downloadedFilesRepository: DownloadedFilesRoomRepository
    abstract val followupsRepository: FollowupsRoomRepository
    abstract val globalActivityRepository: GlobalActivityRoomRepository
    abstract val languagesRepository: LanguagesRoomRepository
    abstract val trainingTipsRepository: TrainingTipsRoomRepository
    abstract val translationsRepository: TranslationsRoomRepository
    abstract val userRepository: UserRoomRepository
    abstract val userCountersRepository: UserCountersRoomRepository
    abstract val lastSyncTimeRepository: LastSyncTimeRoomRepository
    abstract val toolsRepository: ToolsRoomRepository
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
 * v6.2.0
 * 8: 2023-01-24
 * 9: 2023-05-09
 * 10: 2023-05-08
 * 11: 2023-05-15
 * 12: 2023-06-08
 * 13: 2023-09-18
 * 14: 2023-09-18
 * 15: 2023-09-18
 */

internal fun RoomDatabase.Builder<GodToolsRoomDatabase>.enableMigrations() = fallbackToDestructiveMigration()

@RenameColumn(tableName = "tools", fromColumnName = "isAdded", toColumnName = "isFavorite")
internal class Migration14 : AutoMigrationSpec
// endregion Migrations
