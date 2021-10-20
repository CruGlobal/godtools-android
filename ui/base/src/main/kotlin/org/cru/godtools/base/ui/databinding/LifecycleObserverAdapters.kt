package org.cru.godtools.base.ui.databinding

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.databinding.adapters.ListenerUtil
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.cru.godtools.base.ui.R

@BindingAdapter("lifecycleOwner")
internal fun View.observeLifecycle(owner: LifecycleOwner?) {
    if (this !is LifecycleObserver) return

    val old = ListenerUtil.trackListener(this, owner, R.id.lifecycleOwner)
    if (old !== owner) {
        old?.lifecycle?.removeObserver(this)
        owner?.lifecycle?.addObserver(this)
    }
}
