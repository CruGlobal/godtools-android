package org.keynote.godtools.android.db

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.RestrictTo
import androidx.annotation.WorkerThread
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.db.AbstractDao
import org.ccci.gto.android.common.db.CoroutinesAsyncDao
import org.ccci.gto.android.common.db.CoroutinesFlowDao
import org.ccci.gto.android.common.db.LiveDataDao
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Base
import org.cru.godtools.model.Followup
import org.cru.godtools.model.GlobalActivityAnalytics
import org.cru.godtools.model.Language
import org.cru.godtools.model.LocalFile
import org.cru.godtools.model.Tool
import org.cru.godtools.model.TrainingTip
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationFile
import org.keynote.godtools.android.db.Contract.AttachmentTable
import org.keynote.godtools.android.db.Contract.FollowupTable
import org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.Contract.LocalFileTable
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.Contract.TrainingTipTable
import org.keynote.godtools.android.db.Contract.TranslationFileTable
import org.keynote.godtools.android.db.Contract.TranslationTable

@Singleton
class GodToolsDao @Inject internal constructor(
    database: GodToolsDatabase
) : AbstractDao(database), CoroutinesAsyncDao, CoroutinesFlowDao, LiveDataDao {
    init {
        registerType(
            Followup::class.java, FollowupTable.TABLE_NAME, FollowupTable.PROJECTION_ALL, FollowupMapper,
            FollowupTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            Language::class.java, LanguageTable.TABLE_NAME, LanguageTable.PROJECTION_ALL, LanguageMapper,
            LanguageTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            Tool::class.java, ToolTable.TABLE_NAME, ToolTable.PROJECTION_ALL, ToolMapper,
            ToolTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            Attachment::class.java, AttachmentTable.TABLE_NAME, AttachmentTable.PROJECTION_ALL, AttachmentMapper,
            AttachmentTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            Translation::class.java, TranslationTable.TABLE_NAME, TranslationTable.PROJECTION_ALL, TranslationMapper,
            TranslationTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            LocalFile::class.java, LocalFileTable.TABLE_NAME, LocalFileTable.PROJECTION_ALL, LocalFileMapper,
            LocalFileTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            TranslationFile::class.java, TranslationFileTable.TABLE_NAME, TranslationFileTable.PROJECTION_ALL,
            TranslationFileMapper, TranslationFileTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            GlobalActivityAnalytics::class.java, GlobalActivityAnalyticsTable.TABLE_NAME,
            GlobalActivityAnalyticsTable.PROJECTION_ALL, GlobalActivityAnalyticsMapper,
            GlobalActivityAnalyticsTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            TrainingTip::class.java, TrainingTipTable.TABLE_NAME, TrainingTipTable.PROJECTION_ALL, TrainingTipMapper,
            TrainingTipTable.SQL_WHERE_PRIMARY_KEY
        )
    }

    public override fun getPrimaryKeyWhere(obj: Any) = when (obj) {
        is LocalFile -> getPrimaryKeyWhere(LocalFile::class.java, obj.filename)
        is TranslationFile -> getPrimaryKeyWhere(TranslationFile::class.java, obj.translationId, obj.filename)
        is Language -> getPrimaryKeyWhere(Language::class.java, obj.code)
        is Tool -> getPrimaryKeyWhere(Tool::class.java, obj.code!!)
        is TrainingTip -> getPrimaryKeyWhere(TrainingTip::class.java, obj.tool, obj.locale, obj.tipId)
        is Base -> getPrimaryKeyWhere(obj.javaClass, obj.id)
        else -> super.getPrimaryKeyWhere(obj)
    }

    @get:RestrictTo(RestrictTo.Scope.LIBRARY)
    override val coroutineDispatcher get() = super<CoroutinesAsyncDao>.coroutineDispatcher
    @get:RestrictTo(RestrictTo.Scope.LIBRARY)
    override val coroutineScope get() = super<CoroutinesAsyncDao>.coroutineScope

    // region Custom DAO methods
    @WorkerThread
    fun insertNew(obj: Base): Long {
        var attempts = 10
        while (true) {
            obj.initNew()
            try {
                return insert(obj, SQLiteDatabase.CONFLICT_ABORT)
            } catch (e: SQLException) {
                // propagate exception if we've exhausted our attempts
                if (--attempts < 0) throw e
            }
        }
    }

    fun updateSharesDeltaAsync(toolCode: String?, shares: Int) =
        coroutineScope.launch { updateSharesDelta(toolCode, shares) }
    suspend fun updateSharesDelta(toolCode: String?, shares: Int) {
        if (toolCode == null) return
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
