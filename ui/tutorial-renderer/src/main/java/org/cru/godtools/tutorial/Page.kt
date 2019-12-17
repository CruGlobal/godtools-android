package org.cru.godtools.tutorial

import androidx.annotation.LayoutRes

internal enum class Page(
    @LayoutRes val layout: Int,
    val showIndicator: Boolean = true,
    val showMenu: Boolean = true
) {
    ONBOARDING_WELCOME(
        R.layout.tutorial_onboarding_welcome,
        showIndicator = false,
        showMenu = false
    ),
    ONBOARDING_OTHERS(R.layout.tutorial_onboarding_others),
    ONBOARDING_TOOLS(R.layout.tutorial_onboarding_tools),
    ONBOARDING_READY(R.layout.tutorial_onboarding_ready),
    ONBOARDING_FINAL(R.layout.tutorial_onboarding_final, showMenu = false),
    TRAINING_EXPLORE(R.layout.optin_tutorial_explore_slide),
    TRAINING_PREPARE(R.layout.optin_tutorial_prepare_slide),
    TRAINING_TRY(R.layout.optin_tutorial_try_slide),
    TRAINING_MENU(R.layout.optin_tutorial_menu_slide)
}
