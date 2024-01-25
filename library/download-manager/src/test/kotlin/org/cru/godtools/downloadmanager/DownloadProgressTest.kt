package org.cru.godtools.downloadmanager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DownloadProgressTest {
    @Test
    fun testValueCoercion() {
        DownloadProgress(-1, -1).let {
            assertEquals(0, it.progress)
            assertEquals(0, it.max)
            assertTrue(it.isIndeterminate)
        }

        DownloadProgress(20, -1).let {
            assertEquals(0, it.progress)
            assertEquals(0, it.max)
            assertTrue(it.isIndeterminate)
        }

        DownloadProgress(-1, 100).let {
            assertEquals(0, it.progress)
            assertEquals(100, it.max)
            assertFalse(it.isIndeterminate)
        }

        DownloadProgress(20, 5).let {
            assertEquals(5, it.progress)
            assertEquals(5, it.max)
            assertFalse(it.isIndeterminate)
        }
    }

    @Test
    fun testEquals() {
        assertEquals(DownloadProgress.INITIAL, DownloadProgress(0, 0))
        assertEquals(DownloadProgress(10, 20), DownloadProgress(10, 20))
        assertNotEquals(DownloadProgress(9, 20), DownloadProgress(10, 20))
        assertNotEquals(DownloadProgress(9, 19), DownloadProgress(9, 20))
    }

    @Test
    fun testEqualsIndeterminate() {
        assertEquals(DownloadProgress(20, 0), DownloadProgress(10, 0))
    }
}
