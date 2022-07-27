package org.cru.godtools.tutorial.layout.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.VerticalChainReference
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight
import org.cru.godtools.tutorial.R

@Composable
internal fun ConstraintLayoutScope.createTutorialOnboardingPositioning(): TutorialOnboardingPositioning {
    val (title, content, anim) = createRefs()
    val chain = createVerticalChain(title, content, anim, chainStyle = ChainStyle.Packed)

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
                bottom.linkTo(anim.top)
            }
    )
    Spacer(
        modifier = Modifier
            .height(dimensionResource(R.dimen.tutorial_page_onboarding_anim_height))
            .constrainAs(anim) {
                top.linkTo(content.bottom)
                bottom.linkTo(parent.bottom)
            }
    )

    return TutorialOnboardingPositioning(title, content, anim, chain)
}

internal data class TutorialOnboardingPositioning(
    val title: ConstrainedLayoutReference,
    val content: ConstrainedLayoutReference,
    val anim: ConstrainedLayoutReference,
    val chain: VerticalChainReference,
)
