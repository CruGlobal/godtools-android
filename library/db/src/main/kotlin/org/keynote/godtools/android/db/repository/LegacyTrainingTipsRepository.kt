package org.keynote.godtools.android.db.repository

import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.ccci.gto.android.common.db.findAsFlow
import org.cru.godtools.db.repository.TrainingTipsRepository
import org.cru.godtools.model.TrainingTip
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
internal class LegacyTrainingTipsRepository @Inject constructor(val dao: GodToolsDao) : TrainingTipsRepository {
    override fun isTipCompleteFlow(tool: String, locale: Locale, tipId: String) =
        dao.findAsFlow<TrainingTip>(tool, locale, tipId).map { it?.isCompleted == true }
            .distinctUntilChanged()
}
