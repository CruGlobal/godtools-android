package org.cru.godtools.ui.account

import androidx.annotation.StringRes
import org.cru.godtools.R

enum class AccountPage(@StringRes val tabLabel: Int) {
    ACTIVITY(R.string.profile_tab_activity),
    GLOBAL_ACTIVITY(R.string.account_tab_global_activity)
}
