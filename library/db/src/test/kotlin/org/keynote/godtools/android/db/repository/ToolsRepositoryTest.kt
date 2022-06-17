package org.keynote.godtools.android.db.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verifyAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
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
        every { transaction(any(), any<() -> Any>()) } answers { (it.invocation.args[1] as () -> Any).invoke() }
        excludeRecords {
            coroutineDispatcher
            coroutineScope
            transaction(any(), any())
        }
    }
    private val repository = ToolsRepository(dao)

    // region pinTool()/unpinTool()
    private val tool = slot<Tool>()

    @Test
    fun verifyPinTool() = runTest {
        coEvery { dao.coroutineDispatcher } returns UnconfinedTestDispatcher()
        every { dao.update(capture(tool), ToolTable.COLUMN_ADDED) } returns 1

        repository.pinTool(TOOL)
        assertEquals(TOOL, tool.captured.code)
        assertTrue(tool.captured.isAdded)
        verifyAll { dao.update(tool.captured, ToolTable.COLUMN_ADDED) }
    }

    @Test
    fun verifyUnpinTool() = runTest {
        coEvery { dao.coroutineDispatcher } returns UnconfinedTestDispatcher()
        every { dao.update(capture(tool), ToolTable.COLUMN_ADDED) } returns 1

        repository.unpinTool(TOOL)
        assertEquals(TOOL, tool.captured.code)
        assertFalse(tool.captured.isAdded)
        verifyAll { dao.update(tool.captured, ToolTable.COLUMN_ADDED) }
    }
    // endregion pinTool()/unpinTool()
}
