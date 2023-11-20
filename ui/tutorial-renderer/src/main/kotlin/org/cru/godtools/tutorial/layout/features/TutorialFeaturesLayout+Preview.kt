package org.cru.godtools.tutorial.layout.features

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
    widthDp = 5 * 393
)
private fun FeaturesTutorialPreview() = Row {
    PageSet.FEATURES.pages.forEach {
        TutorialPageLayout(it, modifier = Modifier.weight(1f))
    }
}
