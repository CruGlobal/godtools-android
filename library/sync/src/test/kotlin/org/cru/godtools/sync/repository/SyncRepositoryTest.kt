package org.cru.godtools.sync.repository

import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.jsonapi.util.Includes
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.db.repository.UserRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.User
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
        val tool1 = Tool("tool1")
        val tool2 = Tool("tool2")

        syncRepository.storeTools(
            tools = listOf(tool1, tool2),
            existingTools = null,
            includes = Includes()
        )
        coVerifyAll {
            toolsRepository.storeToolsFromSync(match { it.toSet() == setOf(tool1, tool2) })
        }
    }

    @Test
    fun `storeTools() - Don't store invalid tools`() = runTest {
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
    fun `storeTools() - Delete orphaned existing tools`() = runTest {
        val tool1 = Tool("tool1")
        val tool2 = Tool("tool2")

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
        val language = Language().apply {
            id = 1
            code = Locale("lt")
        }

        // run test
        syncRepository.storeLanguage(language)
        coVerifyAll {
            languagesRepository.storeLanguageFromSync(language)
        }
    }

    @Test
    fun `storeLanguage() - Invalid Language`() = runTest {
        val language = Language()

        assertFalse(language.isValid)
        syncRepository.storeLanguage(language)
        verify { languagesRepository wasNot Called }
    }
    // endregion storeLanguage()

    // region storeTranslations()
    @Test
    fun `storeTranslations()`() = runTest {
        val trans1 = Translation("tool", Locale.ENGLISH)
        val trans2 = Translation("tool", Locale.FRENCH)
        val trans3 = Translation("tool", Locale.GERMAN)
        val tool = Tool("tool", translations = listOf(trans1, trans2))
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
    // endregion storeTranslations()

    // region storeUser()
    private val user = User().apply {
        apiFavoriteTools = listOf(
            Tool("a"),
            Tool("b"),
        )
    }

    @Test
    fun `storeUser()`() = runTest {
        syncRepository.storeUser(user, Includes())
        coVerifyAll {
            userRepository.storeUserFromSync(user)
            toolsRepository wasNot Called
        }
    }

    @Test
    fun `storeUser() - Store favorite tools`() = runTest {
        user.isInitialFavoriteToolsSynced = true

        syncRepository.storeUser(user, Includes(User.JSON_FAVORITE_TOOLS))
        coVerifyAll {
            userRepository.storeUserFromSync(user)
            toolsRepository.storeToolsFromSync(user.apiFavoriteTools)
            toolsRepository.storeFavoriteToolsFromSync(user.apiFavoriteTools)
        }
    }

    @Test
    fun `storeUser() - Don't store favorite tools if they haven't been synced yet`() = runTest {
        user.isInitialFavoriteToolsSynced = false

        syncRepository.storeUser(user, Includes(User.JSON_FAVORITE_TOOLS))
        coVerifyAll {
            userRepository.storeUserFromSync(user)
            toolsRepository wasNot Called
        }
    }
    // endregion storeUser()
}
