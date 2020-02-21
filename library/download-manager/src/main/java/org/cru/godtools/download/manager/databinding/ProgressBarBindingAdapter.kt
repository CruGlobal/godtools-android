package org.cru.godtools.download.manager.databinding

import android.os.Build
import android.view.View
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import org.cru.godtools.download.manager.DownloadProgress

@BindingAdapter("android:progress")
fun ProgressBar.bindProgress(download: DownloadProgress?) {
    val oldVisibility = visibility
    visibility = if (download != null) View.VISIBLE else View.GONE

    if (download != null) {
        isIndeterminate = download.isIndeterminate
        max = download.max
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setProgress(download.progress, visibility == oldVisibility)
        } else {
            this.progress = download.progress
        }
    }
}
