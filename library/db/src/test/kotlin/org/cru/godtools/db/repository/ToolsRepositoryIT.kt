package org.cru.godtools.db.repository

import app.cash.turbine.test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.Tool
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ToolsRepositoryIT {
    protected val testScope = TestScope()
    abstract val repository: ToolsRepository

    @Test
    fun verifyPinTool() = testScope.runTest {
        val code = "pinTool"
        repository.insert(Tool(code))

        repository.findToolFlow(code).test {
            assertFalse(assertNotNull(awaitItem()).isAdded)

            repository.pinTool(code)
            assertTrue(assertNotNull(awaitItem()).isAdded)
        }
    }

    @Test
    fun verifyUnpinTool() = testScope.runTest {
        val code = "pinTool"
        repository.insert(Tool(code) { isAdded = true })

        repository.findToolFlow(code).test {
            assertTrue(assertNotNull(awaitItem()).isAdded)

            repository.unpinTool(code)
            assertFalse(assertNotNull(awaitItem()).isAdded)
        }
    }
}
