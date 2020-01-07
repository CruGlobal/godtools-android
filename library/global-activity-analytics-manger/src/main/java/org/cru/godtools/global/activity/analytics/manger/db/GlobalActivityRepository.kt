package org.cru.godtools.global.activity.analytics.manger.db

import androidx.room.Dao
import androidx.room.Transaction
import org.cru.godtools.global.activity.analytics.manger.model.GlobalActivityAnalytics

@Dao
abstract class GlobalActivityRepository internal constructor(private val db: GlobalActivityAnalyticsDatabase) {

    @Transaction
    open fun addOrUpdateGlobalActivity(globalActivity: GlobalActivityAnalytics) {
        db.globalActivityDao().insertOrReplace(globalActivity)
    }
}
