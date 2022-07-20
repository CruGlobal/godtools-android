package org.cru.godtools.ui.banner

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum

@Composable
@Preview(showBackground = true)
private fun SingleLineBanner() = Banner("Single Line", primaryButton = "Confirm", secondaryButton = "Dismiss")

@Composable
@Preview(showBackground = true)
private fun MultiLineBanner() = Banner(
    LoremIpsum(20).values.first().replace("\n", " "),
    primaryButton = "Confirm",
    secondaryButton = "Dismiss",
)

@Composable
@Preview(showBackground = true)
private fun ActionsOnSeparateLinesBanner() = Banner(
    "Short Message",
    primaryButton = "Confirm Primary Action",
    secondaryButton = LoremIpsum(7).values.first(),
)
