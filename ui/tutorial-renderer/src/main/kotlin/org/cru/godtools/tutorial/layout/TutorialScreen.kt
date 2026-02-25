package org.cru.godtools.tutorial.layout

import com.slack.circuit.runtime.screen.PopResult
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import org.cru.godtools.tutorial.PageSet

@Parcelize
data class TutorialScreen(val pageSet: PageSet) : Screen {
    sealed interface Result : PopResult {
        @Parcelize data object Canceled : Result
        @Parcelize data object Finished : Result
        @Parcelize data object ShowQrCode : Result
    }
}
