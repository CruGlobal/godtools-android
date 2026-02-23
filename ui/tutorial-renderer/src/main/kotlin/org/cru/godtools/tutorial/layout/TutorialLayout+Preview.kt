package org.cru.godtools.tutorial.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.layout.TutorialPresenter.UiState

@Composable
@Preview(showBackground = true)
private fun OnboardingTutorial() = TutorialLayout(UiState(PageSet.ONBOARDING))

@Composable
@Preview(showBackground = true)
private fun FeaturesTutorial() = TutorialLayout(UiState(PageSet.FEATURES))

@Composable
@Preview(showBackground = true)
private fun TipsTutorial() = TutorialLayout(UiState(PageSet.TIPS))

@Composable
@Preview(showBackground = true)
private fun LiveShareTutorial() = TutorialLayout(UiState(PageSet.LIVE_SHARE))
