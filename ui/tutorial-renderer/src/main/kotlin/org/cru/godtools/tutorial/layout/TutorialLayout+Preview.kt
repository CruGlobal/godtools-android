package org.cru.godtools.tutorial.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.tutorial.PageSet

@Composable
@Preview(showBackground = true)
private fun OnboardingTutorial() = GodToolsTheme { TutorialLayout(PageSet.ONBOARDING) }

@Composable
@Preview(showBackground = true)
private fun FeaturesTutorial() = GodToolsTheme { TutorialLayout(PageSet.FEATURES) }

@Composable
@Preview(showBackground = true)
private fun TipsTutorial() = GodToolsTheme { TutorialLayout(PageSet.TIPS) }

@Composable
@Preview(showBackground = true)
private fun LiveShareTutorial() = GodToolsTheme { TutorialLayout(PageSet.LIVE_SHARE) }
