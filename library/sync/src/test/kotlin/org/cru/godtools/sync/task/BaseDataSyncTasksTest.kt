package org.cru.godtools.sync.task

import android.database.sqlite.SQLiteDatabase
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import java.util.Locale
import org.ccci.gto.android.common.db.Expression
import org.ccci.gto.android.common.db.Query
import org.cru.godtools.model.Language
import org.junit.Before
import org.junit.Test
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.GodToolsDao

class BaseDataSyncTasksTest {
    private val dao = mockk<GodToolsDao>(relaxUnitFun = true)

    private lateinit var tasks: BaseDataSyncTasks

    @Before
    fun setup() {
        tasks = object : BaseDataSyncTasks(dao) {}
    }

    @Test
    fun `storeLanguage()`() {
        // setup test
        val language = Language().apply {
            id = 1
            code = Locale("lt")
        }
        every { dao.refresh(any()) } returns null
        every { dao.get(any<Query<Language>>()) } returns emptyList()
        excludeRecords {
            dao.refresh(any())
            dao.get(any<Query<Language>>())
        }

        // run test
        tasks.storeLanguage(language)
        verify { dao.updateOrInsert(refEq(language), SQLiteDatabase.CONFLICT_REPLACE, *anyVararg()) }
        confirmVerified(dao)
    }

    @Test
    fun `storeLanguage() - Missing Code`() {
        val language = Language().apply {
            id = 1
        }

        tasks.storeLanguage(language)
        confirmVerified(dao)
    }

    @Test
    fun `storeLanguage() - Changing Code`() {
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
        val pk = mockk<Expression>()
        every { dao.refresh(any()) } returns null
        every { dao.get(any<Query<Language>>()) } returns listOf(originalLanguage)
        every { dao.getPrimaryKeyWhere(originalLanguage) } returns pk
        every { dao.update(language, pk, LanguageTable.COLUMN_CODE) } returns 1
        excludeRecords {
            dao.refresh(any())
            dao.get(any<Query<Language>>())
            dao.getPrimaryKeyWhere(any())
        }

        // run test
        tasks.storeLanguage(language)
        verifyOrder {
            dao.update(refEq(language), refEq(pk), LanguageTable.COLUMN_CODE)
            dao.updateOrInsert(refEq(language), SQLiteDatabase.CONFLICT_REPLACE, *anyVararg())
        }
        confirmVerified(dao)
    }
}
