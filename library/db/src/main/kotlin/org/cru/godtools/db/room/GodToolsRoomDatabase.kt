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
import org.cru.godtools.db.room.entity.PersonalizedToolOrderEntity
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
    version = 26,
    entities = [
        AttachmentEntity::class,
        LanguageEntity::class,
        DownloadedFileEntity::class,
        DownloadedTranslationFileEntity::class,
        FollowupEntity::class,
        GlobalActivityEntity::class,
        ToolEntity::class,
        PersonalizedToolOrderEntity::class,
        TrainingTipEntity::class,
        TranslationEntity::class,
        UserEntity::class,
        UserCounterEntity::class,
        LastSyncTimeEntity::class,
    ],
    autoMigrations = [
        AutoMigration(from = 7, to = 22, spec = Migration7To22::class),
        AutoMigration(from = 22, to = 23),
        AutoMigration(from = 23, to = 24),
        AutoMigration(from = 24, to = 25),
        AutoMigration(from = 25, to = 26),
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
 * v6.1.0-v6.1.1
 * 7: 2023-03-27
 * v6.2.0
 * 22: 2024-04-30
 * v6.3.0
 * 23: 2024-06-13
 * v6.3.1
 * 24: 2024-04-22
 * v6.3.2
 * 25: 2025-01-27
 * v6.3.7
 * 26: 2026-04-07
 */

internal fun RoomDatabase.Builder<GodToolsRoomDatabase>.enableMigrations() =
    fallbackToDestructiveMigration(dropAllTables = true)

@RenameColumn(tableName = "languages", fromColumnName = "id", toColumnName = "apiId")
internal class Migration7To22 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM last_sync_times WHERE id LIKE ?", arrayOf("last_synced.user%"))
    }
}
// endregion Migrations
