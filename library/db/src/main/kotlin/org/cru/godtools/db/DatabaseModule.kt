package org.cru.godtools.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.ThreadUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.DownloadedFilesRepository
import org.cru.godtools.db.repository.FollowupsRepository
import org.cru.godtools.db.repository.GlobalActivityRepository
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.LastSyncTimeRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TrainingTipsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.db.repository.UserCountersRepository
import org.cru.godtools.db.repository.UserRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.enableMigrations
import org.keynote.godtools.android.db.GodToolsDatabase
import org.keynote.godtools.android.db.repository.LegacyDownloadedFilesRepository

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    internal fun roomDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, GodToolsRoomDatabase::class.java, GodToolsRoomDatabase.DATABASE_NAME)
            .enableMigrations()
            .build()

    @Provides
    @Reusable
    internal fun attachmentsRepository(db: GodToolsRoomDatabase, legacyDb: GodToolsDatabase): AttachmentsRepository {
        legacyDb.triggerDataMigration()
        return db.attachmentsRepository
    }

    @Provides
    @Reusable
    internal fun downloadedFilesRepository(
        repository: LegacyDownloadedFilesRepository,
        legacyDb: GodToolsDatabase,
    ): DownloadedFilesRepository {
        legacyDb.triggerDataMigration()
        return repository
    }

    @Provides
    @Reusable
    internal fun languagesRepository(db: GodToolsRoomDatabase, legacyDb: GodToolsDatabase): LanguagesRepository {
        legacyDb.triggerDataMigration()
        return db.languagesRepository
    }

    @Provides
    @Reusable
    internal fun followupsRepository(db: GodToolsRoomDatabase, legacyDb: GodToolsDatabase): FollowupsRepository {
        legacyDb.triggerDataMigration()
        return db.followupsRepository
    }

    @Provides
    @Reusable
    internal fun globalActivityRepository(
        db: GodToolsRoomDatabase,
        legacyDb: GodToolsDatabase,
    ): GlobalActivityRepository {
        legacyDb.triggerDataMigration()
        return db.globalActivityRepository
    }

    @Provides
    @Reusable
    internal fun trainingTipsRepository(db: GodToolsRoomDatabase, legacyDb: GodToolsDatabase): TrainingTipsRepository {
        legacyDb.triggerDataMigration()
        return db.trainingTipsRepository
    }

    @Provides
    @Reusable
    internal fun userRepository(db: GodToolsRoomDatabase): UserRepository = db.userRepository

    @Provides
    @Reusable
    internal fun userCountersRepository(db: GodToolsRoomDatabase, legacyDb: GodToolsDatabase): UserCountersRepository {
        legacyDb.triggerDataMigration()
        return db.userCountersRepository
    }

    @Provides
    @Reusable
    internal fun lastSyncTimeRepository(db: GodToolsRoomDatabase): LastSyncTimeRepository = db.lastSyncTimeRepository

    @Provides
    @Reusable
    internal fun toolsRepository(db: GodToolsRoomDatabase, legacyDb: GodToolsDatabase): ToolsRepository {
        legacyDb.triggerDataMigration()
        return db.toolsRepository
    }

    @Provides
    @Reusable
    internal fun translationsRepository(db: GodToolsRoomDatabase, legacyDb: GodToolsDatabase): TranslationsRepository {
        legacyDb.triggerDataMigration()
        return db.translationsRepository
    }

    private fun GodToolsDatabase.triggerDataMigration() {
        // TODO: eventually this logic will be triggered directly by the roomDatabase singleton,
        //       until then we trigger it before returning a repository that depends on the migrated data
        when {
            ThreadUtil.isMainThread() -> GlobalScope.launch { writableDatabase }
            else -> writableDatabase
        }
    }
}
