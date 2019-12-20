package org.cru.godtools.content

import androidx.annotation.StringRes
import org.cru.godtools.R

enum class ToolHeader(
    @StringRes val descriptionText: Int,
    @StringRes val openText: Int,
    @StringRes val dismissText: Int
) {
    TUTORIAL(R.string.tutorial_open_description, R.string.open_tutorial, R.string.dismiss_tutorial)
}
