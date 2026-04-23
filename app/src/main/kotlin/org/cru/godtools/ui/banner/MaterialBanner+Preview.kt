package org.cru.godtools.ui.banner

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import org.cru.godtools.R

@Composable
@Preview(showBackground = true)
private fun SingleLineMaterialBanner() = MaterialBanner(
    "Single Line",
    primaryButton = "Confirm",
    secondaryButton = "Dismiss"
)

@Composable
@Preview(showBackground = true)
private fun DefaultMaterialBanner() = MaterialBanner(
    LoremIpsum(20).values.first().replace("\n", " "),
    primaryButton = "Confirm",
    secondaryButton = "Dismiss",
)

@Composable
@Preview(showBackground = true)
private fun MaterialBannerWithActionsOnSeparateLines() = MaterialBanner(
    "Short Message",
    primaryButton = "Confirm Primary Action",
    secondaryButton = LoremIpsum(7).values.first(),
)

@Composable
@Preview(showBackground = true)
private fun MaterialBannerIcon() = MaterialBanner(
    LoremIpsum(20).values.first().replace("\n", " "),
    primaryButton = "Confirm",
    secondaryButton = "Dismiss",
    icon = painterResource(R.drawable.ic_favorite_24dp),
    iconTint = Color.Cyan
)

@Composable
@Preview(showBackground = true)
private fun MaterialBannerIconShortText() = MaterialBanner(
    "Single Line",
    primaryButton = "Confirm",
    secondaryButton = "Dismiss",
    icon = painterResource(R.drawable.ic_favorite_24dp),
    iconTint = Color.Cyan
)

@Composable
@Preview(showBackground = true)
private fun MaterialBannerIconShortTextLongActions() = MaterialBanner(
    "Single Line",
    primaryButton = "Confirm Primary Action",
    secondaryButton = "Dismiss Secondary Action",
    icon = painterResource(R.drawable.ic_favorite_24dp),
    iconTint = Color.Cyan
)
