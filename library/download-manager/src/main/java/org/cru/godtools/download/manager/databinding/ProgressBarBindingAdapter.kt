package org.cru.godtools.download.manager.databinding

import android.view.View
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import org.ccci.gto.android.common.compat.view.setProgressCompat
import org.cru.godtools.download.manager.DownloadProgress

@BindingAdapter("android:progress")
fun ProgressBar.bindProgress(download: DownloadProgress?) {
    val oldVisibility = visibility
    visibility = if (download != null) View.VISIBLE else View.GONE

    if (download != null) {
        isIndeterminate = download.isIndeterminate
        max = download.max
        setProgressCompat(download.progress, visibility == oldVisibility)
    }
}
