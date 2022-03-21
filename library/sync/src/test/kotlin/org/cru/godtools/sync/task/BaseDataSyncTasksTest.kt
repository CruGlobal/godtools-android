package org.cru.godtools.sync.task

import android.database.sqlite.SQLiteDatabase
import androidx.collection.SimpleArrayMap
import java.util.Locale
import org.ccci.gto.android.common.db.Expression
import org.ccci.gto.android.common.db.Query
import org.cru.godtools.model.Language
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Test
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.GodToolsDao
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.same
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BaseDataSyncTasksTest {
    private lateinit var dao: GodToolsDao
    private lateinit var eventBus: EventBus
    private lateinit var events: SimpleArrayMap<Class<*>, Any>

    private lateinit var tasks: BaseDataSyncTasks

    @Before
    fun setup() {
        dao = mock()
        eventBus = mock()
        events = mock()
        tasks = object : BaseDataSyncTasks(dao, eventBus) {}
    }

    @Test
    fun verifyStoreLanguage() {
        // setup test
        val language = Language().apply {
            id = 1
            code = Locale("lt")
        }

        // run test
        tasks.storeLanguage(language)
        verify(dao).refresh(same(language))
        verify(dao, never()).update(any(), anyOrNull<Expression>(), any<String>())
        verify(dao).updateOrInsert(same(language), eq(SQLiteDatabase.CONFLICT_REPLACE), any())
    }

    @Test
    fun verifyStoreLanguageChangingCode() {
        // setup test
        val language = Language().apply {
            id = 1
            code = Locale("lt")
            isAdded = false
        }
        val originalLanguage = Language().apply {
            id = 1
            code = Locale.forLanguageTag("lt-LT")
            isAdded = true
        }
        val pk = Expression.raw("test", "")
        whenever(dao.get(any<Query<Language>>())).thenReturn(listOf(originalLanguage))
        whenever(dao.getPrimaryKeyWhere(eq(originalLanguage))).thenReturn(pk)

        // run test
        tasks.storeLanguage(language)
        verify(dao).refresh(same(language))
        verify(dao).update(same(language), same(pk), eq(LanguageTable.COLUMN_CODE))
        verify(dao).updateOrInsert(same(language), eq(SQLiteDatabase.CONFLICT_REPLACE), any())
    }
}
