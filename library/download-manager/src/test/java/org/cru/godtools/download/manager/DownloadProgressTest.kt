package org.cru.godtools.download.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
        assertTrue(DownloadProgress.INITIAL == DownloadProgress.INDETERMINATE)
        assertTrue(DownloadProgress.INDETERMINATE == DownloadProgress(0, 0))
        assertTrue(DownloadProgress(10, 20) == DownloadProgress(10, 20))
        assertFalse(DownloadProgress(9, 20) == DownloadProgress(10, 20))
        assertFalse(DownloadProgress(9, 19) == DownloadProgress(9, 20))
    }
}
