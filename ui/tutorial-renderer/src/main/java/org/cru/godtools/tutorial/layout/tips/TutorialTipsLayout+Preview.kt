package org.cru.godtools.tutorial.layout.tips

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import org.cru.godtools.tutorial.Page

@Composable
@Preview(
    showBackground = true,
    device = Devices.PIXEL_3A,
    widthDp = 3 * 393
)
private fun TipsTutorial() = Row {
    TutorialTipsLayout(page = Page.TIPS_LEARN, modifier = Modifier.weight(1f))
    TutorialTipsLayout(page = Page.TIPS_LIGHT, modifier = Modifier.weight(1f))
    TutorialTipsLayout(page = Page.TIPS_START, modifier = Modifier.weight(1f))
}
