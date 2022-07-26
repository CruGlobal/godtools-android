package org.keynote.godtools.android.db.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
class LessonsRepository @Inject constructor(dao: GodToolsDao) {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    val lessons = Query.select<Tool>()
        .where(Contract.ToolTable.FIELD_TYPE.eq(Tool.Type.LESSON) and Contract.ToolTable.FIELD_HIDDEN.ne(true))
        .orderBy(Contract.ToolTable.COLUMN_DEFAULT_ORDER)
        .getAsFlow(dao)
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(replayExpirationMillis = REPLAY_EXPIRATION), 1)

    val spotlightLessons = Query.select<Tool>()
        .where(
            Contract.ToolTable.FIELD_TYPE.eq(Tool.Type.LESSON) and
                Contract.ToolTable.FIELD_HIDDEN.ne(true) and
                Contract.ToolTable.FIELD_SPOTLIGHT.eq(true)
        )
        .orderBy(Contract.ToolTable.COLUMN_DEFAULT_ORDER)
        .getAsFlow(dao)
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(replayExpirationMillis = REPLAY_EXPIRATION), 1)
}
