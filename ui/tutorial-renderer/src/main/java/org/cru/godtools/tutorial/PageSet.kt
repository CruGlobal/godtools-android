package org.cru.godtools.tutorial

enum class PageSet(internal vararg val pages: Page) {
    BAKED_IN(
        Page.ONBOARDING_WELCOME,
        Page.ONBOARDING_OTHERS,
        Page.ONBOARDING_TOOLS,
        Page.ONBOARDING_READY,
        Page.ONBOARDING_FINAL
    ),
    OPT_IN(
        Page.TRAINING_EXPLORE,
        Page.TRAINING_PREPARE,
        Page.TRAINING_TRY,
        Page.TRAINING_MENU
    );

    companion object {
        val DEFAULT = BAKED_IN
    }
}
