package org.cru.godtools.tutorial.databinding

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.cru.godtools.tutorial.R

@BindingAdapter("lifecycleOwner")
internal fun View.observeLifecycle(owner: LifecycleOwner?) {
    if (this is LifecycleObserver) {
        if (lifecycleOwner == owner) return
        lifecycleOwner?.lifecycle?.removeObserver(this)
        lifecycleOwner = owner
        lifecycleOwner?.lifecycle?.addObserver(this)
    }
}

private var View.lifecycleOwner: LifecycleOwner?
    get() = getTag(R.id.lifecycleOwner) as? LifecycleOwner
    set(value) = setTag(R.id.lifecycleOwner, value)
