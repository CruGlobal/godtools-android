package org.cru.godtools.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.cru.godtools.db.repository.GlobalActivityRepository
import org.cru.godtools.db.repository.LastSyncTimeRepository
import org.cru.godtools.db.repository.UserCountersRepository
import org.cru.godtools.db.repository.UserRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.enableMigrations

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
    fun GodToolsRoomDatabase.globalActivityRepository(): GlobalActivityRepository = globalActivityRepository

    @Provides
    @Reusable
    fun GodToolsRoomDatabase.userRepository(): UserRepository = userRepository

    @Provides
    @Reusable
    fun GodToolsRoomDatabase.userCountersRepository(): UserCountersRepository = userCountersRepository

    @Provides
    @Reusable
    fun GodToolsRoomDatabase.lastSyncTimeRepository(): LastSyncTimeRepository = lastSyncTimeRepository
}
