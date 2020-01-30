package org.cru.godtools.tutorial

import org.cru.godtools.base.Settings

enum class PageSet(
    internal val feature: String? = null,
    internal val pages: List<Page>,
    internal val menu: Int? = null,
    internal val showUpNavigation: Boolean = true,
    internal val analyticsBaseScreenName: String
) {
    ONBOARDING(
        feature = Settings.FEATURE_TUTORIAL_ONBOARDING,
        showUpNavigation = false,
        menu = R.menu.tutorial_onboarding_menu,
        analyticsBaseScreenName = "onboarding",
        pages = listOf(
            Page.ONBOARDING_WELCOME,
            Page.ONBOARDING_OTHERS,
            Page.ONBOARDING_TOOLS,
            Page.ONBOARDING_READY,
            Page.ONBOARDING_FINAL
        )
    ),
    TRAINING(
        feature = Settings.FEATURE_TUTORIAL_TRAINING,
        analyticsBaseScreenName = "tutorial",
        pages = listOf(
            Page.TRAINING_WATCH,
            Page.TRAINING_PREPARE,
            Page.TRAINING_TRY,
            Page.TRAINING_FINAL
        )
    );

    companion object {
        val DEFAULT = ONBOARDING
    }
}
