package org.keynote.godtools.android.db.repository

import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verifyAll
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.db.Query
import org.cru.godtools.model.Tool
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

private const val TOOL = "tool"

@OptIn(ExperimentalCoroutinesApi::class)
class ToolsRepositoryTest {
    private val dao = mockk<GodToolsDao>(relaxUnitFun = true) {
        every { getAsFlow(any<Query<*>>()) } answers { flowOf(emptyList()) }
        excludeRecords {
            getAsFlow(any<Query<*>>())
            transaction(any(), any())
        }
    }

    private inline fun withRepository(body: (ToolsRepository) -> Unit) {
        val repository = ToolsRepository(dao)
        body(repository)
    }

    // region pinTool()/unpinTool()
    private val tool = slot<Tool>()

    @Test
    fun verifyPinTool() = runTest {
        every { dao.updateAsync(capture(tool), ToolTable.COLUMN_ADDED) } returns CompletableDeferred(1)

        withRepository { it.pinTool(TOOL) }
        assertEquals(TOOL, tool.captured.code)
        assertTrue(tool.captured.isAdded)
        verifyAll { dao.updateAsync(tool.captured, ToolTable.COLUMN_ADDED) }
    }

    @Test
    fun verifyUnpinTool() = runTest {
        every { dao.updateAsync(capture(tool), ToolTable.COLUMN_ADDED) } returns CompletableDeferred(1)

        withRepository { it.unpinTool(TOOL) }
        assertEquals(TOOL, tool.captured.code)
        assertFalse(tool.captured.isAdded)
        verifyAll { dao.updateAsync(tool.captured, ToolTable.COLUMN_ADDED) }
    }
    // endregion pinTool()/unpinTool()
}
