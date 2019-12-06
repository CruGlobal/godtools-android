package org.cru.godtools.tutorial

import org.cru.godtools.base.Settings

enum class PageSet(
    internal val feature: String? = null,
    internal val pages: List<Page>
) {
    BAKED_IN(
        feature = Settings.FEATURE_BAKED_IN_TUTORIAL,
        pages = listOf(
            Page.ONBOARDING_WELCOME,
            Page.ONBOARDING_OTHERS,
            Page.ONBOARDING_TOOLS,
            Page.ONBOARDING_READY,
            Page.ONBOARDING_FINAL
        )
    ),
    OPT_IN(
        feature = Settings.FEATURE_OPT_IN_TUTORIAL,
        pages = listOf(
            Page.TRAINING_EXPLORE,
            Page.TRAINING_PREPARE,
            Page.TRAINING_TRY,
            Page.TRAINING_MENU
        )
    );

    companion object {
        val DEFAULT = BAKED_IN
    }
}
