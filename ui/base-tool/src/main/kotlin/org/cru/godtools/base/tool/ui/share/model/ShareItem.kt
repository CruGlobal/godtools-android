package org.cru.godtools.base.tool.ui.share.model

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import org.ccci.gto.android.common.base.Ordered
import org.cru.godtools.base.tool.ui.share.OtherActionsAdapter.Callbacks

interface ShareItem :
    Parcelable,
    Ordered {
    val shareIntent: Intent? get() = null

    @get:LayoutRes
    val actionLayout: Int? get() = null
    fun bindTo(binding: ViewDataBinding, callbacks: Callbacks?) = Unit
    fun triggerAction(activity: Activity)

    val isValid get() = true
}
