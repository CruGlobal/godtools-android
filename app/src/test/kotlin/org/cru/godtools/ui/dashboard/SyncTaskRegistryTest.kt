package org.cru.godtools.ui.dashboard

import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.InternalCircuitApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.coroutines.test.TestScope
import org.ccci.gto.android.common.sync.SyncTracker
import org.cru.godtools.ui.dashboard.SyncTaskRegistry.Companion.syncTaskRegistry

@OptIn(InternalCircuitApi::class)
class SyncTaskRegistryTest {
    private val syncTracker = SyncTracker(TestScope())
    private val registry = SyncTaskRegistry(syncTracker)

    // region registerSyncTask()
    @Test
    fun `registerSyncTask - returns unique IDs`() {
        val id1 = registry.registerSyncTask {}
        val id2 = registry.registerSyncTask {}
        assertNotEquals(id1, id2)
    }

    @Test
    fun `registerSyncTask - immediately executes task with force = false`() {
        val calls = mutableListOf<Boolean>()
        registry.registerSyncTask { force -> calls += force }
        assertEquals(listOf(false), calls)
    }
    // endregion registerSyncTask()

    // region unregisterSyncTask()
    @Test
    fun `unregisterSyncTask - task not called after unregister`() {
        val calls = mutableListOf<Boolean>()
        val id = registry.registerSyncTask { force -> calls += force }
        calls.clear()

        registry.unregisterSyncTask(id)
        registry.triggerSyncTasks()
        assertEquals(emptyList(), calls)
    }

    @Test
    fun `unregisterSyncTask - unknown id is safe`() {
        registry.unregisterSyncTask("unknown-id")
    }
    // endregion unregisterSyncTask()

    // region triggerSyncTasks()
    @Test
    fun `triggerSyncTasks - calls all registered tasks`() {
        val calls1 = mutableListOf<Boolean>()
        val calls2 = mutableListOf<Boolean>()
        registry.registerSyncTask { force -> calls1 += force }
        registry.registerSyncTask { force -> calls2 += force }
        calls1.clear()
        calls2.clear()

        registry.triggerSyncTasks()
        assertEquals(1, calls1.size)
        assertEquals(1, calls2.size)
    }

    @Test
    fun `triggerSyncTasks - passes force = false by default`() {
        val calls = mutableListOf<Boolean>()
        registry.registerSyncTask { force -> calls += force }
        calls.clear()

        registry.triggerSyncTasks()
        assertEquals(listOf(false), calls)
    }

    @Test
    fun `triggerSyncTasks - passes force = true when specified`() {
        val calls = mutableListOf<Boolean>()
        registry.registerSyncTask { force -> calls += force }
        calls.clear()

        registry.triggerSyncTasks(force = true)
        assertEquals(listOf(true), calls)
    }
    // endregion triggerSyncTasks()

    // region CircuitContext.syncTaskRegistry
    @Test
    fun `syncTaskRegistry - null by default`() {
        val context = CircuitContext(null)
        assertNull(context.syncTaskRegistry)
    }

    @Test
    fun `syncTaskRegistry - returns set value`() {
        val context = CircuitContext(null)
        context.syncTaskRegistry = registry
        assertSame(registry, context.syncTaskRegistry)
    }

    @Test
    fun `syncTaskRegistry - traverses parent context`() {
        val parent = CircuitContext(null)
        parent.syncTaskRegistry = registry
        val child = CircuitContext(parent)
        assertSame(registry, child.syncTaskRegistry)
    }

    @Test
    fun `syncTaskRegistry - child value takes precedence over parent`() {
        val parentRegistry = SyncTaskRegistry(syncTracker)
        val parent = CircuitContext(null)
        parent.syncTaskRegistry = parentRegistry
        val child = CircuitContext(parent)
        child.syncTaskRegistry = registry
        assertSame(registry, child.syncTaskRegistry)
    }
    // endregion CircuitContext.syncTaskRegistry
}
