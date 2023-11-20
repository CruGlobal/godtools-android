package org.cru.godtools.tutorial.layout.onboarding

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.layout.TutorialPageLayout

@Composable
@Preview(
    showBackground = true,
    device = Devices.PIXEL_3A,
    widthDp = 4 * 393
)
private fun OnboardingTutorial() = Row {
    PageSet.ONBOARDING.pages.forEach {
        TutorialPageLayout(it, modifier = Modifier.weight(1f))
    }
}
