package org.cru.godtools.tutorial

import androidx.annotation.LayoutRes

internal enum class Page(@LayoutRes val layout: Int) {
    ONBOARDING_WELCOME(R.layout.baked_in_tutorial_welcome),
    ONBOARDING_OTHERS(R.layout.baked_in_tutorial_others),
    ONBOARDING_TOOLS(R.layout.baked_in_tutorial_tools),
    ONBOARDING_READY(R.layout.baked_in_tutorial_ready),
    ONBOARDING_FINAL(R.layout.baked_in_tutorial_final),
    TRAINING_EXPLORE(R.layout.optin_tutorial_explore_slide),
    TRAINING_PREPARE(R.layout.optin_tutorial_prepare_slide),
    TRAINING_TRY(R.layout.optin_tutorial_try_slide),
    TRAINING_MENU(R.layout.optin_tutorial_menu_slide),
}
