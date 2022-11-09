package org.cru.godtools.db.room.repository

import androidx.room.Dao
import java.util.Locale
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import org.cru.godtools.db.repository.TrainingTipsRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.entity.TrainingTipEntity

@Dao
internal abstract class TrainingTipsRoomRepository(private val db: GodToolsRoomDatabase) : TrainingTipsRepository {
    private inline val dao get() = db.trainingTipDao

    override fun getCompletedTipsFlow() = dao.getCompletedTrainingTipsFlow().map { it.map { it.toModel() }.toSet() }

    override suspend fun markTipComplete(tool: String, locale: Locale, tipId: String) {
        val tip = TrainingTipEntity(TrainingTipEntity.Key(tool, locale, tipId))
        tip.isCompleted = true
        dao.insertOrReplace(tip)
    }

    override fun isTipCompleteFlow(tool: String, locale: Locale, tipId: String) =
        dao.findTrainingTipFlow(tool, locale, tipId).map { it?.isCompleted == true }
}
