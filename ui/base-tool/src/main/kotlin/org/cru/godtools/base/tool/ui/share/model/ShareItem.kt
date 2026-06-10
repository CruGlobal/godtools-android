package org.cru.godtools.base.tool.ui.share.model

import android.app.Activity
import android.content.Intent
import android.os.Parcelable

interface ShareItem : Parcelable {
    val shareIntent: Intent? get() = null
    fun triggerAction(activity: Activity)
}
