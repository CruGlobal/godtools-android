package org.cru.godtools.base.tool.ui.settings

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.core.content.res.ResourcesCompat
import org.cru.godtools.tool.R

@Suppress("FunctionName")
fun ShareLinkSettingsAction(context: Context, onClick: () -> Unit) = SettingsActionsAdapter.SettingsAction(
    id = 1,
    label = R.string.tract_settings_action_share,
    labelColor = org.cru.godtools.ui.R.color.white,
    icon = R.drawable.ic_tool_settings_share,
    iconTint = org.cru.godtools.ui.R.color.white,
    background = ColorDrawable(
        ResourcesCompat.getColor(context.resources, org.cru.godtools.ui.R.color.gt_blue, context.theme)
    ),
    onClick = onClick
)
