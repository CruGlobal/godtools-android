package org.cru.godtools.global.activity.analytics.manger.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.ccci.gto.android.common.room.converter.DateConverter
import org.cru.godtools.base.util.SingletonHolder
import org.cru.godtools.global.activity.analytics.manger.model.GlobalActivityAnalytics

internal const val DATABASE_NAME = "global_activity_analytics.db"

@Database(entities = [GlobalActivityAnalytics::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class GlobalActivityAnalyticsDatabase internal constructor() : RoomDatabase() {

    abstract fun globalActivityDao(): GlobalActivityDao

    abstract fun globalActivityRepository(): GlobalActivityAnalyticsDatabase

    companion object : SingletonHolder<GlobalActivityAnalyticsDatabase, Context>({
        Room.databaseBuilder(it.applicationContext, GlobalActivityAnalyticsDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    })
}
