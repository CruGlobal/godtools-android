package org.cru.godtools.tutorial.layout.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayoutScope
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.layout.TutorialPositionReferences

@Composable
internal fun ConstraintLayoutScope.createTutorialOnboardingPositioning(): TutorialPositionReferences {
    val (title, content, media) = createRefs()
    val chain = createVerticalChain(title, content, media, chainStyle = ChainStyle.Packed)

    Spacer(
        modifier = Modifier
            .minLinesHeight(2, MaterialTheme.typography.headlineMedium)
            .constrainAs(title) {
                top.linkTo(parent.top)
                bottom.linkTo(content.top)
            }
    )
    Spacer(
        modifier = Modifier
            .minLinesHeight(3, MaterialTheme.typography.bodyLarge)
            .constrainAs(content) {
                top.linkTo(title.bottom, margin = 12.dp)
                bottom.linkTo(media.top)
            }
    )
    Spacer(
        modifier = Modifier
            .height(dimensionResource(R.dimen.tutorial_page_onboarding_anim_height))
            .constrainAs(media) {
                top.linkTo(content.bottom)
                bottom.linkTo(parent.bottom)
            }
    )

    return TutorialPositionReferences(title, content, media, chain)
}
