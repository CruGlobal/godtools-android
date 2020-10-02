package org.cru.godtools.download.manager

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.cru.godtools.model.Tool
import org.cru.godtools.model.event.ToolUpdateEvent
import org.greenrobot.eventbus.EventBus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

private const val TOOL = "tool"

class GodToolsDownloadManagerTest {
    private lateinit var dao: GodToolsDao
    private lateinit var eventBus: EventBus

    private lateinit var downloadManager: KotlinGodToolsDownloadManager

    @Before
    fun setup() {
        dao = mock()
        eventBus = mock()

        downloadManager = KotlinGodToolsDownloadManager(dao, eventBus)
    }

    // region pinTool()
    @Test
    fun verifyPinTool() {
        runBlocking { downloadManager.pinTool(TOOL) }
        argumentCaptor<Tool> {
            verify(dao).update(capture(), eq(ToolTable.COLUMN_ADDED))
            assertEquals(TOOL, firstValue.code)
            assertTrue(firstValue.isAdded)
        }
        verify(eventBus).post(ToolUpdateEvent)
    }

    @Test
    fun verifyPinToolAsync() {
        downloadManager.pinToolAsync(TOOL)

        runBlocking {
            with(downloadManager.job) {
                complete()
                join()
            }
        }
        argumentCaptor<Tool> {
            verify(dao).update(capture(), eq(ToolTable.COLUMN_ADDED))
            assertEquals(TOOL, firstValue.code)
            assertTrue(firstValue.isAdded)
        }
        verify(eventBus).post(ToolUpdateEvent)
    }
    // endregion pinTool()
}
