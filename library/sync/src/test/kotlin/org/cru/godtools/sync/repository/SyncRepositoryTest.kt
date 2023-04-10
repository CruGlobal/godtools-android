package org.cru.godtools.sync.repository

import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import java.util.Locale
import org.ccci.gto.android.common.jsonapi.util.Includes
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.junit.Assert.assertFalse
import org.junit.Test

class SyncRepositoryTest {
    private val languagesRepository: LanguagesRepository = mockk(relaxUnitFun = true)
    private val toolsRepository: ToolsRepository = mockk(relaxUnitFun = true)

    private val syncRepository = SyncRepository(
        attachmentsRepository = mockk(),
        dao = mockk(),
        languagesRepository = languagesRepository,
        toolsRepository = toolsRepository
    )

    // region storeTools()
    @Test
    fun `storeTools()`() {
        val tool1 = Tool("tool1")
        val tool2 = Tool("tool2")

        syncRepository.storeTools(
            tools = listOf(tool1, tool2),
            existingTools = null,
            includes = Includes()
        )
        verifyAll {
            toolsRepository.storeToolFromSync(tool1)
            toolsRepository.storeToolFromSync(tool2)
        }
    }

    @Test
    fun `storeTools() - Don't store invalid tools`() {
        val tool: Tool = mockk {
            every { isValid } returns false
        }

        syncRepository.storeTools(
            tools = listOf(tool),
            existingTools = null,
            includes = Includes()
        )
        verify { toolsRepository wasNot Called }
    }

    @Test
    fun `storeTools() - Delete orphaned existing tools`() {
        val tool1 = Tool("tool1")
        val tool2 = Tool("tool2")

        syncRepository.storeTools(
            tools = listOf(tool1, tool2),
            existingTools = mutableSetOf("tool1", "orphan"),
            includes = Includes()
        )
        verifyAll {
            toolsRepository.storeToolFromSync(tool1)
            toolsRepository.storeToolFromSync(tool2)
            toolsRepository.deleteIfNotFavoriteBlocking("orphan")
        }
    }
    // endregion storeTools()

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
