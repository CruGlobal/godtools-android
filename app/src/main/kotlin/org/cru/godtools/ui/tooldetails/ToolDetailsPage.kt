package org.cru.godtools.ui.tooldetails

import androidx.annotation.StringRes
import org.cru.godtools.R

internal enum class ToolDetailsPage(@StringRes val tabLabel: Int) {
    DESCRIPTION(R.string.label_tools_about),
    VARIANTS(R.string.tool_details_section_variants_label)
}
