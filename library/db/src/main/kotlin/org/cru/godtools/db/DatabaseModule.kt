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
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    const val LEGACY_DATA_MIGRATION = "JOB_LEGACY_DATA_MIGRATION"

    @Provides
    @Singleton
    internal fun roomDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, GodToolsRoomDatabase::class.java, GodToolsRoomDatabase.DATABASE_NAME)
            .enableMigrations()
            .build()
            .also { it.legacyDataMigration = GodToolsDatabase(context, it).triggerDataMigration() }

    @Provides
    @Reusable
    @Named(LEGACY_DATA_MIGRATION)
    internal fun legacyDataMigration(db: GodToolsRoomDatabase): Job = db.legacyDataMigration

    @Provides
    @Reusable
    internal fun attachmentsRepository(db: GodToolsRoomDatabase): AttachmentsRepository = db.attachmentsRepository

    @Provides
    @Reusable
    internal fun downloadedFilesRepository(db: GodToolsRoomDatabase): DownloadedFilesRepository =
        db.downloadedFilesRepository

    @Provides
    @Reusable
    internal fun languagesRepository(db: GodToolsRoomDatabase): LanguagesRepository = db.languagesRepository

    @Provides
    @Reusable
    internal fun followupsRepository(db: GodToolsRoomDatabase): FollowupsRepository = db.followupsRepository

    @Provides
    @Reusable
    internal fun globalActivityRepository(db: GodToolsRoomDatabase): GlobalActivityRepository =
        db.globalActivityRepository

    @Provides
    @Reusable
    internal fun trainingTipsRepository(db: GodToolsRoomDatabase): TrainingTipsRepository = db.trainingTipsRepository

    @Provides
    @Reusable
    internal fun userRepository(db: GodToolsRoomDatabase): UserRepository = db.userRepository

    @Provides
    @Reusable
    internal fun userCountersRepository(db: GodToolsRoomDatabase): UserCountersRepository = db.userCountersRepository

    @Provides
    @Reusable
    internal fun lastSyncTimeRepository(db: GodToolsRoomDatabase): LastSyncTimeRepository = db.lastSyncTimeRepository

    @Provides
    @Reusable
    internal fun toolsRepository(db: GodToolsRoomDatabase): ToolsRepository = db.toolsRepository

    @Provides
    @Reusable
    internal fun translationsRepository(db: GodToolsRoomDatabase): TranslationsRepository = db.translationsRepository

    private fun GodToolsDatabase.triggerDataMigration(): Job {
        // TODO: eventually this logic will be triggered directly by the roomDatabase singleton,
        //       until then we trigger it before returning a repository that depends on the migrated data
        return when {
            ThreadUtil.isMainThread() -> GlobalScope.launch { writableDatabase }
            else -> {
                writableDatabase
                Job().apply { complete() }
            }
        }
    }
}
