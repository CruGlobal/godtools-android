package org.cru.godtools.download.manager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

class DownloadProgressLiveDataTest {
    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testInitialProgress() {
        val liveData = DownloadProgressLiveData()

        assertNull(liveData.value)
        liveData.value = DownloadProgress.INITIAL
        assertSame(DownloadProgress.INITIAL, liveData.value)

        liveData.value = DownloadProgress(1, 2)
        liveData.value = DownloadProgress.INITIAL
        assertEquals(DownloadProgress(1, 2), liveData.value)

        liveData.value = DownloadProgress.INDETERMINATE
        assertEquals(DownloadProgress.INDETERMINATE, liveData.value)

        liveData.value = null
        liveData.value = DownloadProgress.INITIAL
        assertSame(DownloadProgress.INITIAL, liveData.value)
    }
}
