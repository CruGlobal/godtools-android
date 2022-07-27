package org.cru.godtools.tutorial.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.R

@Composable
internal fun TutorialPageLayout(
    page: Page,
    nextPage: () -> Unit = {},
    onTutorialAction: (Int) -> Unit = {},
) = Box(
    modifier = Modifier
        .padding(
            top = dimensionResource(R.dimen.tutorial_page_inset_top),
            bottom = dimensionResource(R.dimen.tutorial_page_inset_bottom)
        )
) {
    when (page) {
        else -> Unit
    }
}
