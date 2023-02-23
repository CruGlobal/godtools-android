package org.cru.godtools.init.content.task

import android.content.Context
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.io.ByteArrayInputStream
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LastSyncTimeRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.junit.Test
import org.keynote.godtools.android.db.GodToolsDao

@OptIn(ExperimentalCoroutinesApi::class)
class TasksTest {
    private val context = mockk<Context> {
        every { assets } returns mockk {
            every { open(any()) } answers { ByteArrayInputStream(ByteArray(0)) }
        }
    }
    private val dao = mockk<GodToolsDao> {
        every { getLastSyncTime(*anyVararg()) } returns 0
        every { updateLastSyncTime(*anyVararg()) } just Runs
    }
    private val downloadManager = mockk<GodToolsDownloadManager>()
    private val jsonApiConverter = mockk<JsonApiConverter>()
    private val lastSyncTimeRepository: LastSyncTimeRepository = mockk {
        coEvery { getLastSyncTime(*anyVararg()) } returns 0
        coEvery { updateLastSyncTime(*anyVararg()) } just Runs
    }
    private val settings = mockk<Settings> {
        every { primaryLanguage } returns Locale("x")
    }
    private val toolsRepository: ToolsRepository = mockk(relaxUnitFun = true) {
        coEvery { getTools() } returns emptyList()
    }

    private val tasks = Tasks(
        context,
        mockk(),
        dao,
        downloadManager,
        jsonApiConverter,
        languagesRepository = mockk(),
        lastSyncTimeRepository = lastSyncTimeRepository,
        settings = settings,
        toolsRepository = toolsRepository,
        translationsRepository = mockk()
    )

    // region initFavoriteTools()
    @Test
    fun `initFavoriteTools - Already Ran - Last sync recorded`() = runTest {
        coEvery { lastSyncTimeRepository.getLastSyncTime(*anyVararg()) } returns 5
        tasks.initFavoriteTools()
        coVerify {
            lastSyncTimeRepository.getLastSyncTime(*anyVararg())
            downloadManager wasNot Called
            toolsRepository wasNot Called
        }
        confirmVerified(lastSyncTimeRepository)
    }

    @Test
    fun `initFavoriteTools - Already Ran - Has favorite tools`() = runTest {
        coEvery { toolsRepository.getTools() } returns listOf(Tool().apply { isAdded = true })
        tasks.initFavoriteTools()
        coVerify {
            lastSyncTimeRepository.getLastSyncTime(*anyVararg())
            toolsRepository.getTools()
            downloadManager wasNot Called
        }
        confirmVerified(lastSyncTimeRepository, toolsRepository)
    }

    @Test
    fun testInitFavoriteTools() = runTest {
        val tools = Array(5) { Tool("${it + 1}") }
        val translations = listOf("1", "5").map { Translation(it) }
        coEvery { toolsRepository.getTools() } returns tools.toList()
        every { dao.get(any<Query<Translation>>()) } returns translations
        every { jsonApiConverter.fromJson(any(), Tool::class.java) } returns JsonApiObject.of(*tools)

        tasks.initFavoriteTools()
        coVerifyAll {
            toolsRepository.getTools()
            toolsRepository.pinTool("1")
            toolsRepository.pinTool("2")
            toolsRepository.pinTool("3")
            toolsRepository.pinTool("5")
        }
        confirmVerified(toolsRepository)
    }
    // endregion initFavoriteTools()

    private fun Tool(tool: String) = Tool().apply { code = tool }
    private fun Translation(tool: String) = Translation().apply { toolCode = tool }
}
