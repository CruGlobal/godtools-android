package org.cru.godtools.sync.task

import android.database.sqlite.SQLiteDatabase
import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import java.util.Locale
import org.cru.godtools.model.Language
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
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
        val language = Language().apply {
            id = 1
            code = Locale("lt")
        }

        // run test
        tasks.storeLanguage(language)
        verifyAll {
            dao.updateOrInsert(language, SQLiteDatabase.CONFLICT_REPLACE, *anyVararg())
        }
    }

    @Test
    fun `storeLanguage() - Invalid Language`() {
        val language = Language()

        assertFalse(language.isValid)
        tasks.storeLanguage(language)
        verify { dao wasNot Called }
    }
}
