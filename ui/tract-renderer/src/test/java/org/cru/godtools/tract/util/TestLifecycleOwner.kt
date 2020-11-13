package org.cru.godtools.tract.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

// TODO: replace this with AndroidX TestLifecycleOwner in "lifecycle-runtime-testing:2.3.0"
class TestLifecycleOwner : LifecycleOwner {
    val lifecycleRegistry = LifecycleRegistry(this)
    override fun getLifecycle(): Lifecycle = lifecycleRegistry
}
