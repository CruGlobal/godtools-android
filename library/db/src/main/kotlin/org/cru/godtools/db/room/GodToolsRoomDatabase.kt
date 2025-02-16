package org.cru.godtools.db.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
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
import org.cru.godtools.db.room.entity.DownloadedTranslationFileEntity
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
    version = 24,
    entities = [
        AttachmentEntity::class,
        LanguageEntity::class,
        DownloadedFileEntity::class,
        DownloadedTranslationFileEntity::class,
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
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 12, to = 13),
        AutoMigration(from = 13, to = 14, spec = Migration14::class),
        AutoMigration(from = 14, to = 15),
        AutoMigration(from = 15, to = 16, spec = ResetUserSyncMigration::class),
        AutoMigration(from = 16, to = 17),
        AutoMigration(from = 17, to = 18, spec = Migration18::class),
        AutoMigration(from = 18, to = 19, spec = Migration19::class),
        AutoMigration(from = 19, to = 20),
        AutoMigration(from = 20, to = 21),
        AutoMigration(from = 21, to = 22),
        AutoMigration(from = 22, to = 23),
        AutoMigration(from = 23, to = 24),
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
 * 16: 2023-09-19
 * 17: 2023-09-25
 * 18: 2023-11-21
 * 19: 2023-12-07
 * 20: 2024-01-17
 * 21: 2024-01-26
 * 22: 2024-04-30
 * v6.3.0
 * 23: 2024-06-13
 * v6.3.1
 * 24: 2024-04-22
 */

internal fun RoomDatabase.Builder<GodToolsRoomDatabase>.enableMigrations() = fallbackToDestructiveMigration()

internal class ResetUserSyncMigration : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM last_sync_times WHERE id LIKE ?", arrayOf("last_synced.user%"))
    }
}

@RenameColumn(tableName = "tools", fromColumnName = "isAdded", toColumnName = "isFavorite")
internal class Migration14 : AutoMigrationSpec

@RenameColumn(tableName = "languages", fromColumnName = "id", toColumnName = "apiId")
internal class Migration18 : AutoMigrationSpec

@RenameColumn(tableName = "tools", fromColumnName = "id", toColumnName = "apiId")
internal class Migration19 : AutoMigrationSpec
// endregion Migrations
