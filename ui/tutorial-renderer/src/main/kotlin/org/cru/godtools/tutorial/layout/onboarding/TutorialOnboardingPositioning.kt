package org.cru.godtools.tutorial.layout.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
            .constrainAs(title) {
                top.linkTo(parent.top)
                bottom.linkTo(content.top)
            }
            .minLinesHeight(2, MaterialTheme.typography.headlineMedium)
    )
    Spacer(
        modifier = Modifier
            .constrainAs(content) {
                top.linkTo(title.bottom)
                bottom.linkTo(media.top)
            }
            .padding(top = 12.dp)
            .minLinesHeight(3, MaterialTheme.typography.bodyLarge)
    )
    Spacer(
        modifier = Modifier
            .constrainAs(media) {
                top.linkTo(content.bottom)
                bottom.linkTo(parent.bottom)
            }
            .height(dimensionResource(R.dimen.tutorial_page_onboarding_anim_height))
    )

    return TutorialPositionReferences(title, content, media, chain)
}
