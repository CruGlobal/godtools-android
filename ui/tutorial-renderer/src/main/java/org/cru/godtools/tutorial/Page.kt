package org.cru.godtools.tutorial

import androidx.annotation.LayoutRes

internal enum class Page(
    @LayoutRes val layout: Int,
    val screenName: String,
    val showIndicator: Boolean = true,
    val showMenu: Boolean = true
) {
    ONBOARDING_WELCOME(
        R.layout.tutorial_onboarding_welcome,
        screenName = "onboarding-1",
        showIndicator = false,
        showMenu = false
    ),
    ONBOARDING_OTHERS(R.layout.tutorial_onboarding_others, screenName = "onboarding-2"),
    ONBOARDING_TOOLS(R.layout.tutorial_onboarding_tools, screenName = "onboarding-3"),
    ONBOARDING_READY(R.layout.tutorial_onboarding_ready, screenName = "onboarding-4"),
    ONBOARDING_FINAL(R.layout.tutorial_onboarding_final, screenName = "onboarding-5", showMenu = false),
    TRAINING_WATCH(R.layout.tutorial_training_watch, screenName = "tutorial-1"),
    TRAINING_PREPARE(R.layout.tutorial_training_prepare, screenName = "tutorial-2"),
    TRAINING_TRY(R.layout.tutorial_training_try, screenName = "tutorial-3"),
    TRAINING_FINAL(R.layout.tutorial_training_final, screenName = "tutorial-4")
}
