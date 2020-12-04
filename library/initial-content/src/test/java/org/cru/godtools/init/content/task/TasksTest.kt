package org.cru.godtools.init.content.task

import android.content.Context
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyVararg
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.cru.godtools.base.Settings
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Translation
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Test
import org.keynote.godtools.android.db.GodToolsDao

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
        context = mock()
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
        verifyZeroInteractions(downloadManager)
    }

    @Test
    fun testInitFavoriteToolsHasDesiredAmountOfLastTools() = runBlockingTest {
        val available = PREFERRED_FAVORITES.takeLast(NUMBER_OF_FAVORITES)
        whenever(dao.get(any<Query<*>>())).thenReturn(available.map { Translation(it) })

        tasks.initFavoriteTools()
        available.forEach { verify(downloadManager).pinTool(it) }
        verifyNoMoreInteractions(downloadManager)
    }
    // endregion initFavoriteTools()

    private fun Translation(tool: String) = Translation().apply { toolCode = tool }
}
