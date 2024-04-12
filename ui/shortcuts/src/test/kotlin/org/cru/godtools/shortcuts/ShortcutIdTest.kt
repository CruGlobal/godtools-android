package org.cru.godtools.shortcuts

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ShortcutIdTest {
    @Test
    fun `parseId() - Tool shortcuts`() {
        assertNull(ShortcutId.parseId("tool"))
        assertEquals(ShortcutId.Tool("kgp"), ShortcutId.parseId("tool|kgp"))
        assertEquals(ShortcutId.Tool("kgp"), ShortcutId.parseId("tool|kgp|en"))
    }

    @Test
    fun `parseId() - Invalid`() {
        assertNull(ShortcutId.parseId("invalid-asldf"))
    }

    @Test
    fun `Tool - id`() {
        assertEquals("tool|kgp", ShortcutId.Tool("kgp").id)
    }
}
