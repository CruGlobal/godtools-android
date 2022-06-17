package org.cru.godtools.init.content.task

import android.content.Context
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coVerifyAll
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.io.ByteArrayInputStream
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.base.Settings
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.junit.Before
import org.junit.Test
import org.keynote.godtools.android.db.GodToolsDao
import org.keynote.godtools.android.db.repository.ToolsRepository

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
    private val settings = mockk<Settings> {
        every { primaryLanguage } returns Locale("x")
    }
    private val toolsRepository = mockk<ToolsRepository>(relaxUnitFun = true)

    private lateinit var tasks: Tasks

    @Before
    fun setup() {
        tasks = Tasks(context, dao, downloadManager, jsonApiConverter, settings, toolsRepository)
    }

    // region initFavoriteTools()
    @Test
    fun testInitFavoriteToolsAlreadyRun() = runTest {
        every { dao.getLastSyncTime(*anyVararg()) } returns 5
        tasks.initFavoriteTools()
        verify {
            dao.getLastSyncTime(*anyVararg())
            downloadManager wasNot Called
            toolsRepository wasNot Called
        }
        confirmVerified(dao)
    }

    @Test
    fun testInitFavoriteTools() = runTest {
        val tools = Array(5) { Tool("${it + 1}") }
        val translations = listOf("1", "5").map { Translation(it) }
        every { dao.get(any<Query<Translation>>()) } returns translations
        every { jsonApiConverter.fromJson(any(), Tool::class.java) } returns JsonApiObject.of(*tools)

        tasks.initFavoriteTools()
        coVerifyAll {
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
