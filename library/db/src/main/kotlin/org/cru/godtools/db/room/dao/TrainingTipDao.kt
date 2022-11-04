package org.cru.godtools.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.db.room.entity.TrainingTipEntity

@Dao
internal interface TrainingTipDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(tip: TrainingTipEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnoreBlocking(tip: TrainingTipEntity)

    @Query("SELECT * FROM training_tips WHERE tool = :tool AND locale = :locale AND tipId = :tipId")
    fun findTrainingTipFlow(tool: String, locale: Locale, tipId: String): Flow<TrainingTipEntity?>
}
