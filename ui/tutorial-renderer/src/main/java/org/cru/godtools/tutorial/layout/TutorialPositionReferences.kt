package org.cru.godtools.tutorial.layout

import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.VerticalChainReference

internal data class TutorialPositionReferences(
    val title: ConstrainedLayoutReference,
    val content: ConstrainedLayoutReference,
    val media: ConstrainedLayoutReference,
    val chain: VerticalChainReference,
)
