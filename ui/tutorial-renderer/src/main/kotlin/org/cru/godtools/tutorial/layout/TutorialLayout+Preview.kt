package org.cru.godtools.tutorial.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.layout.TutorialPresenter.UiState

@Composable
@Preview(showBackground = true)
private fun FeaturesTutorial() = GodToolsTheme { TutorialLayout(UiState(PageSet.FEATURES)) }

@Composable
@Preview(showBackground = true)
private fun TipsTutorial() = GodToolsTheme { TutorialLayout(UiState(PageSet.TIPS)) }

@Composable
@Preview(showBackground = true)
private fun LiveShareTutorial() = GodToolsTheme { TutorialLayout(UiState(PageSet.LIVE_SHARE)) }
