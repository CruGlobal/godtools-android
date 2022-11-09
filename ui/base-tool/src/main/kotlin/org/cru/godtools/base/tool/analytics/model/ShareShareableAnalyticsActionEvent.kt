package org.cru.godtools.base.tool.analytics.model

import androidx.core.os.bundleOf
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.shared.tool.parser.model.shareable.Shareable
import org.cru.godtools.shared.tool.parser.model.shareable.ShareableImage
import org.cru.godtools.shared.user.activity.UserCounterNames

private const val ACTION_SHARE_SHAREABLE = "share_shareable"
private const val PARAM_SHAREABLE_ID = "cru_shareable_id"

class ShareShareableAnalyticsActionEvent(private val shareable: Shareable) :
    ToolAnalyticsActionEvent(shareable.manifest.code, ACTION_SHARE_SHAREABLE, locale = shareable.manifest.locale) {
    override fun isForSystem(system: AnalyticsSystem) = when (system) {
        AnalyticsSystem.USER -> shareable is ShareableImage
        else -> super.isForSystem(system)
    }

    override val firebaseParams get() = bundleOf(PARAM_SHAREABLE_ID to shareable.id)

    override val userCounterName = when (shareable) {
        is ShareableImage -> UserCounterNames.IMAGE_SHARED
        else -> null
    }
}
