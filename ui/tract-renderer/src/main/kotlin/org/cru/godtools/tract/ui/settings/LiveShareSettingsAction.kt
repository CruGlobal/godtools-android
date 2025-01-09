package org.cru.godtools.tract.ui.settings

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.core.content.res.ResourcesCompat
import org.cru.godtools.base.tool.ui.settings.SettingsActionsAdapter
import org.cru.godtools.tool.tract.R

@Suppress("FunctionName")
fun LiveShareSettingsAction(context: Context, onClick: () -> Unit) = SettingsActionsAdapter.SettingsAction(
    id = 101,
    label = R.string.menu_live_share_publish,
    icon = R.drawable.ic_tract_settings_live_share,
    iconTint = org.cru.godtools.ui.R.color.gt_blue,
    background = ColorDrawable(
        ResourcesCompat.getColor(context.resources, org.cru.godtools.ui.R.color.gray_F5, null)
    ),
    onClick = onClick
)
