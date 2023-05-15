package org.keynote.godtools.android.db

import androidx.annotation.RestrictTo
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.db.AbstractDao
import org.ccci.gto.android.common.db.CoroutinesAsyncDao
import org.ccci.gto.android.common.db.CoroutinesFlowDao
import org.ccci.gto.android.common.db.LiveDataDao
import org.cru.godtools.model.Base
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationFile
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.Contract.TranslationFileTable
import org.keynote.godtools.android.db.Contract.TranslationTable

@Singleton
class GodToolsDao @Inject internal constructor(
    database: GodToolsDatabase
) : AbstractDao(database), CoroutinesAsyncDao, CoroutinesFlowDao, LiveDataDao {
    init {
        registerType(
            Tool::class.java,
            ToolTable.TABLE_NAME,
            ToolTable.PROJECTION_ALL,
            ToolMapper,
            ToolTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            Translation::class.java,
            TranslationTable.TABLE_NAME,
            TranslationTable.PROJECTION_ALL,
            TranslationMapper,
            TranslationTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            TranslationFile::class.java,
            TranslationFileTable.TABLE_NAME,
            TranslationFileTable.PROJECTION_ALL,
            TranslationFileMapper,
            TranslationFileTable.SQL_WHERE_PRIMARY_KEY
        )
    }

    public override fun getPrimaryKeyWhere(obj: Any) = when (obj) {
        is TranslationFile -> getPrimaryKeyWhere(TranslationFile::class.java, obj.translationId, obj.filename)
        is Tool -> getPrimaryKeyWhere(Tool::class.java, obj.code!!)
        is Base -> getPrimaryKeyWhere(obj.javaClass, obj.id)
        else -> super.getPrimaryKeyWhere(obj)
    }

    @get:RestrictTo(RestrictTo.Scope.LIBRARY)
    override val coroutineDispatcher get() = super<CoroutinesAsyncDao>.coroutineDispatcher
    @get:RestrictTo(RestrictTo.Scope.LIBRARY)
    override val coroutineScope get() = super<CoroutinesAsyncDao>.coroutineScope

    // region Custom DAO methods
    internal suspend fun updateSharesDelta(toolCode: String, shares: Int) {
        if (shares == 0) return

        // build query
        val where = compileExpression(getPrimaryKeyWhere(Tool::class.java, toolCode))
        val sql = """
            UPDATE ${getTable(Tool::class.java)}
            SET ${ToolTable.COLUMN_PENDING_SHARES} = coalesce(${ToolTable.COLUMN_PENDING_SHARES}, 0) + ?
            WHERE ${where.sql}
        """
        val args = bindValues(shares) + where.args

        // execute query
        withContext(coroutineDispatcher) {
            transaction(exclusive = false) { db ->
                db.execSQL(sql, args)
                invalidateClass(Tool::class.java)
            }
        }
    }
    // endregion Custom DAO methods
}
