package org.cru.godtools.download.manager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.cru.godtools.download.manager.DownloadProgress.Companion.INDETERMINATE_VAL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

class DownloadProgressLiveDataTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testInitialProgress() {
        val liveData = DownloadProgressLiveData()
        assertNull(liveData.value)

        liveData.value = DownloadProgress.INITIAL
        assertSame(
            "You should be able to set the initial progress if no progress was currently set",
            DownloadProgress.INITIAL,
            liveData.value
        )

        liveData.value = DownloadProgress(1, 2)
        assertEquals(DownloadProgress(1, 2), liveData.value)

        liveData.value = DownloadProgress.INITIAL
        assertEquals(
            "Initial DownloadProgress shouldn't override other download progress",
            DownloadProgress(1, 2),
            liveData.value
        )

        val indeterminate = DownloadProgress(INDETERMINATE_VAL, INDETERMINATE_VAL)
        assertEquals(DownloadProgress.INITIAL, indeterminate)
        liveData.value = indeterminate
        assertEquals("You should still be able to set an indeterminate download state", indeterminate, liveData.value)

        liveData.value = null
        liveData.value = DownloadProgress.INITIAL
        assertSame(
            "You can set the DownloadProgress to initial if the progress is cleared with null first",
            DownloadProgress.INITIAL,
            liveData.value
        )
    }
}
