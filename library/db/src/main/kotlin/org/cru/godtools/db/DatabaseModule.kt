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
import org.cru.godtools.db.repository.TrainingTipsRepository
import org.cru.godtools.db.repository.UserCountersRepository
import org.cru.godtools.db.repository.UserRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.enableMigrations
import org.keynote.godtools.android.db.GodToolsDatabase
import org.keynote.godtools.android.db.repository.LegacyAttachmentsRepository
import org.keynote.godtools.android.db.repository.LegacyDownloadedFilesRepository

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun roomDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, GodToolsRoomDatabase::class.java, GodToolsRoomDatabase.DATABASE_NAME)
            .enableMigrations()
            .build()

    @Provides
    @Reusable
    fun LegacyAttachmentsRepository.attachmentsRepository(): AttachmentsRepository = this

    @Provides
    @Reusable
    fun LegacyDownloadedFilesRepository.downloadedFilesRepository(): DownloadedFilesRepository = this

    @Provides
    @Reusable
    fun GodToolsRoomDatabase.languagesRepository(legacyDb: GodToolsDatabase): LanguagesRepository {
        legacyDb.triggerDataMigration()
        return languagesRepository
    }

    @Provides
    @Reusable
    fun GodToolsRoomDatabase.followupsRepository(legacyDb: GodToolsDatabase): FollowupsRepository {
        legacyDb.triggerDataMigration()
        return followupsRepository
    }

    @Provides
    @Reusable
    fun GodToolsRoomDatabase.globalActivityRepository(legacyDb: GodToolsDatabase): GlobalActivityRepository {
        legacyDb.triggerDataMigration()
        return globalActivityRepository
    }

    @Provides
    @Reusable
    fun GodToolsRoomDatabase.trainingTipsRepository(legacyDb: GodToolsDatabase): TrainingTipsRepository {
        legacyDb.triggerDataMigration()
        return trainingTipsRepository
    }

    @Provides
    @Reusable
    fun GodToolsRoomDatabase.userRepository(): UserRepository = userRepository

    @Provides
    @Reusable
    fun GodToolsRoomDatabase.userCountersRepository(legacyDb: GodToolsDatabase): UserCountersRepository {
        legacyDb.triggerDataMigration()
        return userCountersRepository
    }

    @Provides
    @Reusable
    fun GodToolsRoomDatabase.lastSyncTimeRepository(): LastSyncTimeRepository = lastSyncTimeRepository

    private fun GodToolsDatabase.triggerDataMigration() {
        // TODO: eventually this logic will be triggered directly by the roomDatabase singleton,
        //       until then we trigger it before returning a repository that depends on the migrated data
        when {
            ThreadUtil.isMainThread() -> GlobalScope.launch { writableDatabase }
            else -> writableDatabase
        }
    }
}
