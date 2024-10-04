package org.cru.godtools.tool.tips.ui.settings

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import org.cru.godtools.base.tool.ui.settings.SettingsActionsAdapter
import org.cru.godtools.tool.tips.R

@Suppress("FunctionName")
fun ToggleTipsSettingsAction(context: Context, tipsEnabled: Boolean, onClick: () -> Unit) =
    SettingsActionsAdapter.SettingsAction(
        id = 101,
        label = when {
            tipsEnabled -> R.string.tool_settings_action_tips_disable
            else -> R.string.tool_settings_action_tips_enable
        },
        icon = if (tipsEnabled) R.drawable.ic_disable_tips else R.drawable.ic_tips_tip_done,
        background = ResourcesCompat.getDrawable(context.resources, R.drawable.bkg_tips_tip_done, context.theme),
        onClick = onClick
    )
