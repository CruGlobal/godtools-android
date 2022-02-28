package org.cru.godtools.base.tool.ui.share.model

import android.app.Activity
import android.content.Intent
import kotlinx.parcelize.Parcelize
import org.ccci.gto.android.common.Ordered

@Parcelize
class DefaultShareItem(override val shareIntent: Intent) : ShareItem {
    override val order get() = Ordered.HIGHEST_PRECEDENCE

    override fun triggerAction(activity: Activity) {
        activity.startActivity(Intent.createChooser(shareIntent, null))
    }
}
