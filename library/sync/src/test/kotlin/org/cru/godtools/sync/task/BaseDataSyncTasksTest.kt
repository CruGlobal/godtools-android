package org.cru.godtools.sync.task

import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import java.util.Locale
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Language
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class BaseDataSyncTasksTest {
    private val languagesRepository: LanguagesRepository = mockk(relaxUnitFun = true)

    private lateinit var tasks: BaseDataSyncTasks

    @Before
    fun setup() {
        tasks = object : BaseDataSyncTasks(mockk(), languagesRepository) {}
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
            languagesRepository.storeLanguageFromSync(language)
        }
    }

    @Test
    fun `storeLanguage() - Invalid Language`() {
        val language = Language()

        assertFalse(language.isValid)
        tasks.storeLanguage(language)
        verify { languagesRepository wasNot Called }
    }
}
