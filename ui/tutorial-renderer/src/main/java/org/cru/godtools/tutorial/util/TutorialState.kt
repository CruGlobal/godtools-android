package org.cru.godtools.tutorial.util

import org.cru.godtools.tutorial.Page

enum class TutorialState(internal vararg val pages: Page) {
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
