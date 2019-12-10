package org.cru.godtools.tutorial

import org.cru.godtools.base.Settings

enum class PageSet(
    internal val feature: String? = null,
    internal val pages: List<Page>
) {
    ONBOARDING(
        feature = Settings.FEATURE_TUTORIAL_ONBOARDING,
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
        pages = listOf(
            Page.TRAINING_EXPLORE,
            Page.TRAINING_PREPARE,
            Page.TRAINING_TRY,
            Page.TRAINING_MENU
        )
    );

    companion object {
        val DEFAULT = ONBOARDING
    }
}
