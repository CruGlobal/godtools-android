package org.keynote.godtools.android.db

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.WorkerThread
import com.annimon.stream.Optional
import com.annimon.stream.Stream
import org.ccci.gto.android.common.db.LiveDataDao
import org.ccci.gto.android.common.db.LiveDataRegistry
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.StreamDao
import org.ccci.gto.android.common.db.StreamDao.StreamHelper
import org.ccci.gto.android.common.db.async.AbstractAsyncDao
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Base
import org.cru.godtools.model.Followup
import org.cru.godtools.model.GlobalActivityAnalytics
import org.cru.godtools.model.Language
import org.cru.godtools.model.LocalFile
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationFile
import org.keynote.godtools.android.db.Contract.AttachmentTable
import org.keynote.godtools.android.db.Contract.FollowupTable
import org.keynote.godtools.android.db.Contract.GlobalActivityAnalyticsTable
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.Contract.LocalFileTable
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.Contract.TranslationFileTable
import org.keynote.godtools.android.db.Contract.TranslationTable
import java.util.Locale

abstract class GodToolsDaoKotlin protected constructor(context: Context) :
    AbstractAsyncDao(GodToolsDatabase.getInstance(context)), LiveDataDao, StreamDao {
    override val liveDataRegistry = LiveDataRegistry()

    init {
        registerType(
            Followup::class.java, FollowupTable.TABLE_NAME, FollowupTable.PROJECTION_ALL, FollowupMapper(),
            FollowupTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            Language::class.java, LanguageTable.TABLE_NAME, LanguageTable.PROJECTION_ALL, LanguageMapper,
            LanguageTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            Tool::class.java, ToolTable.TABLE_NAME, ToolTable.PROJECTION_ALL, ToolMapper(),
            ToolTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            Attachment::class.java, AttachmentTable.TABLE_NAME, AttachmentTable.PROJECTION_ALL, AttachmentMapper(),
            AttachmentTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            Translation::class.java, TranslationTable.TABLE_NAME, TranslationTable.PROJECTION_ALL, TranslationMapper(),
            TranslationTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            LocalFile::class.java, LocalFileTable.TABLE_NAME, LocalFileTable.PROJECTION_ALL, LocalFileMapper,
            LocalFileTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            TranslationFile::class.java, TranslationFileTable.TABLE_NAME, TranslationFileTable.PROJECTION_ALL,
            TranslationFileMapper(), TranslationFileTable.SQL_WHERE_PRIMARY_KEY
        )
        registerType(
            GlobalActivityAnalytics::class.java, GlobalActivityAnalyticsTable.TABLE_NAME,
            GlobalActivityAnalyticsTable.PROJECTION_ALL, GlobalActivityAnalyticsMapper,
            GlobalActivityAnalyticsTable.SQL_WHERE_PRIMARY_KEY
        )
    }

    override fun getPrimaryKeyWhere(obj: Any) = when (obj) {
        is LocalFile -> getPrimaryKeyWhere(LocalFile::class.java, obj.fileName!!)
        is TranslationFile -> getPrimaryKeyWhere(TranslationFile::class.java, obj.translationId, obj.fileName!!)
        is Language -> getPrimaryKeyWhere(Language::class.java, obj.code)
        is Tool -> getPrimaryKeyWhere(Tool::class.java, obj.code!!)
        is Base -> getPrimaryKeyWhere(obj.javaClass, obj.id)
        else -> super.getPrimaryKeyWhere(obj)
    }

    @WorkerThread
    override fun <T> streamCompat(query: Query<T>): Stream<T> = StreamHelper.stream(this, query)

    override fun onInvalidateClass(clazz: Class<*>) {
        super.onInvalidateClass(clazz)
        liveDataRegistry.invalidate(clazz)
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
            update(tool, where = null, projection = *arrayOf(ToolTable.COLUMN_ORDER))

            // set order for each specified tool
            tools.forEachIndexed { index, toolId ->
                tool.order = index
                update(tool, ToolTable.FIELD_ID.eq(toolId), ToolTable.COLUMN_ORDER)
            }
        }
    }

    @WorkerThread
    fun getLatestTranslation(code: String?, locale: Locale?): Optional<Translation> {
        if (code == null || locale == null) return Optional.empty()
        return streamCompat(
            Query.select<Translation>()
                .where(TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(code, locale))
                .orderBy(TranslationTable.SQL_ORDER_BY_VERSION_DESC)
                .limit(1)
        ).findFirst()
    }

    @WorkerThread
    fun updateSharesDelta(toolCode: String?, shares: Int) {
        if (toolCode == null) return
        if (shares == 0) return

        // build query
        val where = compileExpression(getPrimaryKeyWhere(Tool::class.java, toolCode))
        val sql = """
            UPDATE ${getTable(Tool::class.java)}
            SET ${ToolTable.COLUMN_PENDING_SHARES} = coalesce(${ToolTable.COLUMN_PENDING_SHARES}, 0) + ?
            WHERE ${where.first}
        """
        val args = bindValues(shares) + where.second

        // execute query
        transaction(exclusive = false) { db ->
            db.execSQL(sql, args)
            invalidateClass(Tool::class.java)
        }
    }
    // endregion Custom DAO methods
}
