package org.cru.godtools.global.activity.analytics.manger.db

import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.cru.godtools.global.activity.analytics.manger.model.GlobalActivityAnalytics

@Dao
interface GlobalActivityDao {

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(globalActivity: GlobalActivityAnalytics)

    @AnyThread
    @Query("SELECT * FROM global_activity_analytics WHERE id = 1")
    fun getGlobalActivityLiveData(): LiveData<GlobalActivityAnalytics>

    @AnyThread
    @Query("SELECT * FROM global_activity_analytics WHERE id = 1")
    fun getGlobalActivity(): GlobalActivityAnalytics
}
