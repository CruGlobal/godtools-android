package org.cru.godtools.download.manager

import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData

internal class DownloadProgressLiveData : MutableLiveData<DownloadProgress?>() {
    @MainThread
    override fun setValue(value: DownloadProgress?) {
        // Only update the progress if this isn't the initial state or we don't currently have progress
        if (value !== DownloadProgress.INITIAL || this.value == null) super.setValue(value)
    }
}
