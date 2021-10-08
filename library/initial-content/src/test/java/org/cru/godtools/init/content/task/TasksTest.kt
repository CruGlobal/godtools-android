package org.cru.godtools.init.content.task

import android.content.Context
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyVararg
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import java.io.ByteArrayInputStream
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.base.Settings
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Test
import org.keynote.godtools.android.db.GodToolsDao
import org.mockito.Mockito.RETURNS_DEEP_STUBS

@OptIn(ExperimentalCoroutinesApi::class)
class TasksTest {
    private lateinit var context: Context
    private lateinit var dao: GodToolsDao
    private lateinit var downloadManager: GodToolsDownloadManager
    private lateinit var eventBus: EventBus
    private lateinit var jsonApiConverter: JsonApiConverter
    private lateinit var settings: Settings

    private lateinit var tasks: Tasks

    @Before
    fun setup() {
        context = mock(defaultAnswer = RETURNS_DEEP_STUBS) {
            on { assets.open(any()) } doReturn ByteArrayInputStream(ByteArray(0))
        }
        dao = mock {
            on { getLastSyncTime(anyVararg()) } doReturn 0
        }
        downloadManager = mock()
        eventBus = mock()
        jsonApiConverter = mock()
        settings = mock {
            on { primaryLanguage } doReturn Locale("x")
        }

        tasks = Tasks(context, dao, downloadManager, jsonApiConverter, settings, eventBus)
    }

    // region initFavoriteTools()
    @Test
    fun testInitFavoriteToolsAlreadyRun() = runBlockingTest {
        whenever(dao.getLastSyncTime(anyVararg())).thenReturn(5)
        tasks.initFavoriteTools()
        verify(dao, never()).get(any<Query<*>>())
        verifyNoMoreInteractions(downloadManager)
    }

    @Test
    fun testInitFavoriteTools() = runBlockingTest {
        val tools = listOf("1", "2", "3", "4", "5").map { Tool(it) }
        val translations = listOf("1", "5").map { Translation(it) }
        whenever(dao.get(any<Query<*>>())).thenReturn(translations)
        whenever(jsonApiConverter.fromJson(any(), eq(Tool::class.java)))
            .thenReturn(JsonApiObject.of(*tools.toTypedArray()))

        tasks.initFavoriteTools()
        verify(downloadManager).pinTool("1")
        verify(downloadManager).pinTool("2")
        verify(downloadManager).pinTool("3")
        verify(downloadManager).pinTool("5")
        verifyNoMoreInteractions(downloadManager)
    }
    // endregion initFavoriteTools()

    private fun Tool(tool: String) = Tool().apply { code = tool }
    private fun Translation(tool: String) = Translation().apply { toolCode = tool }
}
