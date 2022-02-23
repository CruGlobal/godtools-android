package org.cru.godtools.base.tool.ui.share.model

import android.app.Activity
import android.content.Intent
import kotlinx.parcelize.Parcelize
import org.ccci.gto.android.common.Ordered
import org.cru.godtools.base.tool.R

@Parcelize
class DefaultShareItem(override val shareTitle: String? = null, override val shareIntent: Intent) : ShareItem {
    override val order get() = Ordered.HIGHEST_PRECEDENCE

    override fun triggerAction(activity: Activity) {
        activity.startActivity(
            Intent.createChooser(shareIntent, activity.getString(R.string.share_tool_title, shareTitle))
        )
    }
}
