package org.cru.godtools.tutorial

import androidx.annotation.LayoutRes

internal enum class Page(
    @LayoutRes val layout: Int,
    val showIndicator: Boolean = true,
    val showMenu: Boolean = true,
    val showHomeLink: Boolean = true
) {
    ONBOARDING_WELCOME(
        R.layout.baked_in_tutorial_welcome,
        showIndicator = false,
        showMenu = false,
        showHomeLink = false
    ),
    ONBOARDING_OTHERS(
        R.layout.baked_in_tutorial_others,
        showHomeLink = false
    ),
    ONBOARDING_TOOLS(
        R.layout.baked_in_tutorial_tools,
        showHomeLink = false
    ),
    ONBOARDING_READY(
        R.layout.baked_in_tutorial_ready,
        showHomeLink = false
    ),
    ONBOARDING_FINAL(
        R.layout.baked_in_tutorial_final,
        showMenu = false,
        showHomeLink = false
    ),
    TRAINING_EXPLORE(
        R.layout.optin_tutorial_explore_slide,
        showMenu = false
    ),
    TRAINING_PREPARE(
        R.layout.optin_tutorial_prepare_slide,
        showMenu = false
    ),
    TRAINING_TRY(
        R.layout.optin_tutorial_try_slide,
        showMenu = false
    ),
    TRAINING_MENU(
        R.layout.optin_tutorial_menu_slide,
        showMenu = false
    )
}
