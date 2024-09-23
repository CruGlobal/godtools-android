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
import io.mockk.slot
import java.io.ByteArrayInputStream
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.LastSyncTimeRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.downloadmanager.GodToolsDownloadManager
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation

class TasksTest {
    private val context: Context = mockk {
        every { assets } returns mockk {
            every { open(any()) } answers { ByteArrayInputStream(ByteArray(0)) }
        }
    }
    private val downloadManager: GodToolsDownloadManager = mockk()
    private val jsonApiConverter: JsonApiConverter = mockk()
    private val lastSyncTimeRepository: LastSyncTimeRepository = mockk {
        coEvery { getLastSyncTime(*anyVararg()) } returns 0
        coEvery { updateLastSyncTime(*anyVararg()) } just Runs
    }
    private val settings: Settings = mockk {
        every { appLanguage } returns Locale("x")
    }
    private val attachmentsRepository: AttachmentsRepository = mockk {
        coEvery { getAttachments() } returns emptyList()
    }
    private val toolsRepository: ToolsRepository = mockk(relaxUnitFun = true) {
        coEvery { getNormalTools() } returns emptyList()
    }
    private val translationsRepository: TranslationsRepository = mockk()

    private val tasks = Tasks(
        context,
        attachmentsRepository = attachmentsRepository,
        downloadManager,
        jsonApiConverter,
        languagesRepository = mockk(),
        lastSyncTimeRepository = lastSyncTimeRepository,
        settings = settings,
        toolsRepository = toolsRepository,
        translationsRepository = translationsRepository
    )

    // region initFavoriteTools()
    @Test
    fun `initFavoriteTools() - Already Ran - Last sync recorded`() = runTest {
        coEvery { lastSyncTimeRepository.getLastSyncTime(*anyVararg()) } returns 5
        tasks.initFavoriteTools(tasks.bundledData())
        coVerify {
            lastSyncTimeRepository.getLastSyncTime(*anyVararg())
            downloadManager wasNot Called
            toolsRepository wasNot Called
        }
        confirmVerified(lastSyncTimeRepository)
    }

    @Test
    fun `initFavoriteTools() - Already Ran - Has favorite tools`() = runTest {
        coEvery { toolsRepository.getNormalTools() } returns listOf(Tool("tool", isFavorite = true))
        tasks.initFavoriteTools(tasks.bundledData())
        coVerify {
            lastSyncTimeRepository.getLastSyncTime(*anyVararg())
            toolsRepository.getNormalTools()
            downloadManager wasNot Called
        }
        confirmVerified(lastSyncTimeRepository, toolsRepository)
    }

    @Test
    fun `initFavoriteTools()`() = runTest {
        val tools = Array(5) { randomTool("${it + 1}", Tool.Type.TRACT, isFavorite = false, apiId = it.toLong()) }
        val translations = listOf("1", "5").map { randomTranslation(toolCode = it) }
        coEvery { toolsRepository.getNormalTools() } returns tools.toList()
        coEvery { translationsRepository.getTranslationsForLanguages(any()) } returns translations
        every { jsonApiConverter.fromJson(any(), Tool::class.java) } returns JsonApiObject.of(*tools)

        tasks.initFavoriteTools(tasks.bundledData())
        coVerifyAll {
            toolsRepository.getNormalTools()
            toolsRepository.pinTool("1", trackChanges = false)
            toolsRepository.pinTool("2", trackChanges = false)
            toolsRepository.pinTool("3", trackChanges = false)
            toolsRepository.pinTool("5", trackChanges = false)
        }
        confirmVerified(toolsRepository)
    }
    // endregion initFavoriteTools()

    // region loadBundledAttachments()
    private val storedAttachments = slot<Collection<Attachment>>()

    @Test
    fun `loadBundledAttachments()`() = runTest {
        val tools = Array(5) {
            randomTool(type = Tool.Type.TRACT, apiId = it.toLong(), apiAttachments = List(5) { Attachment() })
        }
        every { jsonApiConverter.fromJson(any(), Tool::class.java) } returns JsonApiObject.of(*tools)
        coEvery { attachmentsRepository.storeInitialAttachments(capture(storedAttachments)) } just Runs

        tasks.loadBundledAttachments(tasks.bundledData())
        coVerifyAll {
            attachmentsRepository.getAttachments()
            attachmentsRepository.storeInitialAttachments(any())
        }
        assertEquals(25, storedAttachments.captured.size)
    }

    @Test
    fun `loadBundledAttachments() - Already Ran - Has attachments`() = runTest {
        val attachments = List(5) { Attachment() }
        coEvery { attachmentsRepository.getAttachments() } returns attachments

        tasks.loadBundledAttachments(tasks.bundledData())
        coVerifyAll { attachmentsRepository.getAttachments() }
    }
    // endregion loadBundledAttachments()
}
