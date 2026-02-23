package org.cru.godtools.tutorial

import java.util.Locale
import org.ccci.gto.android.common.androidx.compose.material3.ui.appbar.AppBarAction
import org.cru.godtools.base.Settings
import org.cru.godtools.tutorial.layout.TutorialPresenter.UiEvent

enum class PageSet(
    internal val feature: String? = null,
    private val pages: List<Page>,
    internal val menu: List<Pair<AppBarAction, UiEvent>> = emptyList(),
    internal val showUpNavigation: Boolean = true,
    internal val showPageIndicator: Boolean = true,
    internal val analyticsBaseScreenName: String,
) {
    ONBOARDING(
        feature = Settings.FEATURE_TUTORIAL_ONBOARDING,
        showUpNavigation = false,
        menu = listOf(AppBarAction(titleRes = R.string.tutorial_onboarding_action_skip) to UiEvent.Onboarding.Skip),
        analyticsBaseScreenName = "onboarding",
        pages = listOf(
            Page.ONBOARDING_WELCOME,
            Page.ONBOARDING_CONVERSATIONS,
            Page.ONBOARDING_PREPARE,
            Page.ONBOARDING_SHARE,
        )
    ),
    FEATURES(
        feature = Settings.FEATURE_TUTORIAL_FEATURES,
        analyticsBaseScreenName = "tutorial",
        pages = listOf(
            Page.FEATURES_LESSONS,
            Page.FEATURES_TOOLS,
            Page.FEATURES_TIPS,
            Page.FEATURES_LIVE_SHARE,
            Page.FEATURES_FINAL
        ),
    ),
    LIVE_SHARE(
        menu = listOf(AppBarAction(titleRes = R.string.tutorial_live_share_action_skip) to UiEvent.LiveShare.Skip),
        // TODO: we probably need a better analytics base screen name
        analyticsBaseScreenName = "tutorial-live-share",
        pages = listOf(
            Page.LIVE_SHARE_DESCRIPTION,
            Page.LIVE_SHARE_MIRRORED,
            Page.LIVE_SHARE_START
        )
    ),
    LIVE_SHARE_START_PAGE_ONLY(
        analyticsBaseScreenName = "tutorial-live-share-short",
        pages = listOf(
            Page.LIVE_SHARE_START
        ),
        showPageIndicator = false,
    ),
    TIPS(
        menu = listOf(AppBarAction(titleRes = R.string.tutorial_tips_action_skip) to UiEvent.Tips.Skip),
        analyticsBaseScreenName = "tutorial-tips",
        pages = listOf(
            Page.TIPS_LEARN,
            Page.TIPS_LIGHT,
            Page.TIPS_START
        )
    );

    internal fun pagesFor(locale: Locale) = pages.filter { it.supportsLocale(locale) }

    companion object {
        val DEFAULT = ONBOARDING
    }
}
