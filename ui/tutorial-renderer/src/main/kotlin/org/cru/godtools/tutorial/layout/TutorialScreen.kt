package org.cru.godtools.tutorial.layout

import com.slack.circuit.runtime.screen.PopResult
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import org.cru.godtools.tutorial.PageSet

@Parcelize
data class TutorialScreen(val pageSet: PageSet) : Screen {
    @Parcelize
    data class Result(val resultCode: Int) : PopResult
}
