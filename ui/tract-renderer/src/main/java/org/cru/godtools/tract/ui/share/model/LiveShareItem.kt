package org.cru.godtools.tract.ui.share.model

import android.app.Activity
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.cru.godtools.base.tool.ui.share.model.ShareItem
import org.cru.godtools.tract.R
import org.cru.godtools.tract.activity.TractActivity

@Parcelize
class LiveShareItem(override val shareTitle: String?) : ShareItem {
    @IgnoredOnParcel
    override val actionLayout = R.layout.tract_share_item_live_share

    override fun triggerAction(activity: Activity) {
        (activity as? TractActivity)?.shareLiveShareLink()
    }
}
