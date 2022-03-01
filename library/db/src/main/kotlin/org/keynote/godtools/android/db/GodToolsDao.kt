package org.keynote.godtools.android.db

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.db.AbstractDao
import org.ccci.gto.android.common.db.CoroutinesAsyncDao
import org.ccci.gto.android.common.db.CoroutinesFlowDao
import org.ccci.gto.android.common.db.LiveDataDao
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.get
import org.ccci.gto.android.common.db.getAsFlow
import org.ccci.gto.android.common.db.getAsLiveData
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
import org.cru.godtools.model.UserCounter
import org.keynote.godtools.android.db.Contract.AttachmentTable
import org.keynote.godtools.android.db.Contract.FollowupTable
import org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.Contract.LocalFileTable
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.Contract.TrainingTipTable
import org.keynote.godtools.android.db.Contract.TranslationFileTable
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.Contract.UserCounterTable

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
        registerType(
            UserCounter::class.java, UserCounterTable.TABLE_NAME, UserCounterTable.PROJECTION_ALL, UserCounterMapper,
            UserCounterTable.SQL_WHERE_PRIMARY_KEY
        )
    }

    public override fun getPrimaryKeyWhere(obj: Any) = when (obj) {
        is LocalFile -> getPrimaryKeyWhere(LocalFile::class.java, obj.filename)
        is TranslationFile -> getPrimaryKeyWhere(TranslationFile::class.java, obj.translationId, obj.filename)
        is Language -> getPrimaryKeyWhere(Language::class.java, obj.code)
        is Tool -> getPrimaryKeyWhere(Tool::class.java, obj.code!!)
        is TrainingTip -> getPrimaryKeyWhere(TrainingTip::class.java, obj.tool, obj.locale, obj.tipId)
        is UserCounter -> getPrimaryKeyWhere(UserCounter::class.java, obj.id)
        is Base -> getPrimaryKeyWhere(obj.javaClass, obj.id)
        else -> super.getPrimaryKeyWhere(obj)
    }

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

    @WorkerThread
    fun updateToolOrder(vararg tools: Long) {
        val tool = Tool()
        transaction(exclusive = false) { _ ->
            update(tool, null, ToolTable.COLUMN_ORDER)

            // set order for each specified tool
            tools.forEachIndexed { index, toolId ->
                tool.order = index
                update(tool, ToolTable.FIELD_ID.eq(toolId), ToolTable.COLUMN_ORDER)
            }
        }
    }

    private fun getLatestTranslationQuery(code: String, locale: Locale, isPublished: Boolean, isDownloaded: Boolean) =
        Query.select<Translation>()
            .where(
                TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(code, locale)
                    .run { if (isPublished) and(TranslationTable.SQL_WHERE_PUBLISHED) else this }
                    .run { if (isDownloaded) and(TranslationTable.SQL_WHERE_DOWNLOADED) else this }
            )
            .orderBy(TranslationTable.SQL_ORDER_BY_VERSION_DESC)
            .limit(1)

    @WorkerThread
    fun getLatestTranslation(
        code: String?,
        locale: Locale?,
        isPublished: Boolean = false,
        isDownloaded: Boolean = false
    ): Translation? = when {
        code == null || locale == null -> null
        else -> getLatestTranslationQuery(code, locale, isPublished, isDownloaded).get(this).firstOrNull()
    }

    @AnyThread
    fun getLatestTranslationFlow(
        code: String?,
        locale: Locale?,
        isPublished: Boolean = true,
        isDownloaded: Boolean = false,
        trackAccess: Boolean = false
    ): Flow<Translation?> {
        if (code == null || locale == null) return flowOf(null)
        if (trackAccess) {
            val obj = Translation().apply { updateLastAccessed() }
            val where = TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(code, locale)
            @Suppress("DeferredResultUnused")
            updateAsync(obj, where, TranslationTable.COLUMN_LAST_ACCESSED)
        }
        return getLatestTranslationQuery(code, locale, isPublished, isDownloaded)
            .getAsFlow(this).map { it.firstOrNull() }
    }

    @MainThread
    fun getLatestTranslationLiveData(
        code: String?,
        locale: Locale?,
        isPublished: Boolean = true,
        isDownloaded: Boolean = false,
        trackAccess: Boolean = false
    ): LiveData<Translation?> {
        if (code == null || locale == null) return emptyLiveData()
        if (trackAccess) {
            val obj = Translation().apply { updateLastAccessed() }
            val where = TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(code, locale)
            @Suppress("DeferredResultUnused")
            updateAsync(obj, where, TranslationTable.COLUMN_LAST_ACCESSED)
        }
        return getLatestTranslationQuery(code, locale, isPublished, isDownloaded)
            .getAsLiveData(this).map { it.firstOrNull() }
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

    // region User Counters
    @WorkerThread
    fun updateUserCounterDelta(counterId: String, change: Int) {
        if (change == 0) return

        // build query
        val where = compileExpression(getPrimaryKeyWhere(UserCounter::class.java, counterId))
        val sql = """
            UPDATE ${getTable(UserCounter::class.java)}
            SET ${UserCounterTable.COLUMN_DELTA} = coalesce(${UserCounterTable.COLUMN_DELTA}, 0) + ?
            WHERE ${where.sql}
        """
        val args = bindValues(change) + where.args

        transaction(exclusive = false) { db ->
            db.execSQL(sql, args)
            invalidateClass(UserCounter::class.java)
        }
    }
    // endregion User Counters
    // endregion Custom DAO methods
}
