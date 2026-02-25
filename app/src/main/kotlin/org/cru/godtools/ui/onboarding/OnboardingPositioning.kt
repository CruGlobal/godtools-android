package org.cru.godtools.ui.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.VerticalChainReference
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight

internal val ONBOARDING_MEDIA_HEIGHT = 268.dp

internal data class OnboardingPositionReferences(
    val title: ConstrainedLayoutReference,
    val content: ConstrainedLayoutReference,
    val media: ConstrainedLayoutReference,
    val chain: VerticalChainReference,
)

@Composable
internal fun ConstraintLayoutScope.createOnboardingPositioning(): OnboardingPositionReferences {
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
            .height(ONBOARDING_MEDIA_HEIGHT)
    )

    return OnboardingPositionReferences(title, content, media, chain)
}
