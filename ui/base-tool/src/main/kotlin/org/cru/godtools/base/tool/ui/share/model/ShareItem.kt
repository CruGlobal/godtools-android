package org.cru.godtools.base.tool.ui.share.model

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import androidx.annotation.LayoutRes
import org.ccci.gto.android.common.Ordered

interface ShareItem :
    Parcelable,
    Ordered {
    val shareIntent: Intent? get() = null

    @get:LayoutRes
    val actionLayout: Int? get() = null
    fun triggerAction(activity: Activity)

    val isValid get() = true
}
