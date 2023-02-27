package org.cru.godtools.sync.repository

import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import java.util.Locale
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Language
import org.junit.Assert.assertFalse
import org.junit.Test

class SyncRepositoryTest {
    private val languagesRepository: LanguagesRepository = mockk(relaxUnitFun = true)

    private val syncRepository = SyncRepository(
        attachmentsRepository = mockk(),
        dao = mockk(),
        languagesRepository = languagesRepository,
        toolsRepository = mockk()
    )

    @Test
    fun `storeLanguage()`() {
        val language = Language().apply {
            id = 1
            code = Locale("lt")
        }

        // run test
        syncRepository.storeLanguage(language)
        verifyAll {
            languagesRepository.storeLanguageFromSync(language)
        }
    }

    @Test
    fun `storeLanguage() - Invalid Language`() {
        val language = Language()

        assertFalse(language.isValid)
        syncRepository.storeLanguage(language)
        verify { languagesRepository wasNot Called }
    }
}
