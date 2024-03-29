package org.cru.godtools.sync.repository

import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.jsonapi.util.Includes
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.db.repository.UserRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.User
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation
import org.junit.Assert.assertFalse
import org.junit.Test

class SyncRepositoryTest {
    private val languagesRepository: LanguagesRepository = mockk(relaxUnitFun = true)
    private val toolsRepository: ToolsRepository = mockk(relaxUnitFun = true)
    private val translationsRepository: TranslationsRepository = mockk(relaxUnitFun = true)
    private val userRepository: UserRepository = mockk(relaxUnitFun = true)

    private val syncRepository = SyncRepository(
        attachmentsRepository = mockk(),
        languagesRepository = languagesRepository,
        toolsRepository = toolsRepository,
        translationsRepository = translationsRepository,
        userRepository = userRepository,
    )

    // region storeTools()
    @Test
    fun `storeTools()`() = runTest {
        val tool1 = randomTool("tool1", Tool.Type.TRACT, apiId = 1)
        val tool2 = randomTool("tool2", Tool.Type.TRACT, apiId = 2)

        syncRepository.storeTools(
            tools = listOf(tool1, tool2),
            existingTools = null,
            includes = Includes()
        )
        assertTrue(tool1.isValid)
        assertTrue(tool2.isValid)
        coVerifyAll {
            toolsRepository.storeToolsFromSync(match { it.toSet() == setOf(tool1, tool2) })
        }
    }

    @Test
    fun `storeTools() - Don't store invalid tools`() = runTest {
        val tool = randomTool("", Tool.Type.UNKNOWN, apiId = null)

        syncRepository.storeTools(
            tools = listOf(tool),
            existingTools = null,
            includes = Includes()
        )
        assertFalse(tool.isValid)
        verify { toolsRepository wasNot Called }
    }

    @Test
    fun `storeTools() - Delete orphaned existing tools`() = runTest {
        val tool1 = randomTool("tool1", Tool.Type.TRACT, apiId = 1)
        val tool2 = randomTool("tool2", Tool.Type.TRACT, apiId = 2)

        syncRepository.storeTools(
            tools = listOf(tool1, tool2),
            existingTools = mutableSetOf("tool1", "orphan"),
            includes = Includes()
        )
        coVerifyAll {
            toolsRepository.storeToolsFromSync(match { it.toSet() == setOf(tool1, tool2) })
            toolsRepository.deleteIfNotFavorite("orphan")
        }
    }
    // endregion storeTools()

    // region storeLanguage()
    @Test
    fun `storeLanguage()`() = runTest {
        val language = Language(Locale("lt"))

        // run test
        syncRepository.storeLanguage(language)
        coVerifyAll {
            languagesRepository.storeLanguageFromSync(language)
        }
    }

    @Test
    fun `storeLanguage() - Invalid Language`() = runTest {
        val language = Language(code = Language.INVALID_CODE)

        assertFalse(language.isValid)
        syncRepository.storeLanguage(language)
        verify { languagesRepository wasNot Called }
    }
    // endregion storeLanguage()

    // region storeTranslations()
    @Test
    fun `storeTranslations()`() = runTest {
        val trans1 = randomTranslation("tool", Locale.ENGLISH)
        val trans2 = randomTranslation("tool", Locale.FRENCH)
        val trans3 = randomTranslation("tool", Locale.GERMAN)
        val tool = Tool("tool", Tool.Type.TRACT, apiId = 1, translations = listOf(trans1, trans2))
        coEvery { translationsRepository.getTranslationsForTool("tool") } returns listOf(trans1, trans3)

        syncRepository.storeTools(listOf(tool), null, Includes(Tool.JSON_LATEST_TRANSLATIONS))
        coVerifyAll {
            toolsRepository.storeToolsFromSync(match { it.toSet() == setOf(tool) })
            translationsRepository.getTranslationsForTool("tool")
            translationsRepository.storeTranslationFromSync(trans1)
            translationsRepository.storeTranslationFromSync(trans2)
            translationsRepository.deleteTranslationIfNotDownloaded(trans3.id)
        }
    }

    @Test
    fun `storeTranslations() - Skip invalid translations`() = runTest {
        val transValid = randomTranslation("tool", Locale.ENGLISH)
        val transInvalid = randomTranslation("tool", Language.INVALID_CODE)
        val tool = Tool("tool", Tool.Type.TRACT, apiId = 1, translations = listOf(transValid, transInvalid))
        coEvery { translationsRepository.getTranslationsForTool("tool") } returns emptyList()

        assertTrue(transValid.isValid)
        assertFalse(transInvalid.isValid)
        syncRepository.storeTools(listOf(tool), null, Includes(Tool.JSON_LATEST_TRANSLATIONS))
        coVerifyAll {
            translationsRepository.getTranslationsForTool("tool")
            translationsRepository.storeTranslationFromSync(transValid)
        }
    }
    // endregion storeTranslations()

    // region storeUser()
    @Test
    fun `storeUser()`() = runTest {
        val user = User().apply {
            apiFavoriteTools = listOf(
                Tool("a"),
                Tool("b"),
            )
        }

        syncRepository.storeUser(user, Includes())
        coVerifyAll {
            userRepository.storeUserFromSync(user)
            toolsRepository wasNot Called
        }
    }

    @Test
    fun `storeUser() - Store favorite tools`() = runTest {
        val user = User(isInitialFavoriteToolsSynced = true).apply {
            apiFavoriteTools = listOf(
                randomTool("a", Tool.Type.TRACT, apiId = 1),
                randomTool("b", Tool.Type.TRACT, apiId = 2),
            )
        }

        syncRepository.storeUser(user, Includes(User.JSON_FAVORITE_TOOLS))
        coVerifyAll {
            userRepository.storeUserFromSync(user)
            toolsRepository.storeToolsFromSync(user.apiFavoriteTools)
            toolsRepository.storeFavoriteToolsFromSync(user.apiFavoriteTools)
        }
    }

    @Test
    fun `storeUser() - Don't store favorite tools if they haven't been synced yet`() = runTest {
        val user = User(isInitialFavoriteToolsSynced = false).apply {
            apiFavoriteTools = listOf(
                randomTool("a", Tool.Type.TRACT, apiId = 1),
                randomTool("b", Tool.Type.TRACT, apiId = 2),
            )
        }

        syncRepository.storeUser(user, Includes(User.JSON_FAVORITE_TOOLS))
        coVerifyAll {
            userRepository.storeUserFromSync(user)
            toolsRepository wasNot Called
        }
    }
    // endregion storeUser()
}
