package org.cru.godtools.tutorial.layout.liveshare

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
    TutorialLiveShareLayout(page = Page.LIVE_SHARE_DESCRIPTION, modifier = Modifier.weight(1f))
    TutorialLiveShareLayout(page = Page.LIVE_SHARE_MIRRORED, modifier = Modifier.weight(1f))
    TutorialLiveShareLayout(page = Page.LIVE_SHARE_START, modifier = Modifier.weight(1f))
}
