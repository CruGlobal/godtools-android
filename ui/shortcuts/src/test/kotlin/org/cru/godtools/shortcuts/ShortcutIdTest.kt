package org.cru.godtools.shortcuts

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShortcutIdTest {
    @Test
    fun `ShortcutId - parseId() - Tool shortcuts`() {
        assertNull(ShortcutId.parseId("tool"))
        assertEquals(ShortcutId.Tool("kgp"), ShortcutId.parseId("tool|kgp"))
        assertEquals(ShortcutId.Tool("kgp", Locale.ENGLISH), ShortcutId.parseId("tool|kgp|en"))
    }

    @Test
    fun `ShortcutId - parseId() - Invalid`() {
        assertNull(ShortcutId.parseId("invalid-asldf"))
    }

    @Test
    fun `Tool - id`() {
        assertEquals("tool|kgp", ShortcutId.Tool("kgp").id)
        assertEquals("tool|kgp|en|fr", ShortcutId.Tool("kgp", Locale.ENGLISH, Locale.FRENCH).id)
    }

    @Test
    fun `Tool - parseId() - Valid`() {
        assertNotNull(ShortcutId.Tool.parseId("tool|kgp")) {
            assertEquals("kgp", it.tool)
            assertEquals(emptyList(), it.locales)
            assertTrue(it.isFavoriteToolShortcut)
        }
        assertNotNull(ShortcutId.Tool.parseId("tool|kgp|en|fr")) {
            assertEquals("kgp", it.tool)
            assertEquals(listOf(Locale.ENGLISH, Locale.FRENCH), it.locales)
            assertFalse(it.isFavoriteToolShortcut)
        }
    }

    @Test
    fun `Tool - parseId() - Invalid`() {
        assertNull(ShortcutId.Tool.parseId("tool"))
        assertNull(ShortcutId.Tool.parseId("other|kgp"))
    }
}
