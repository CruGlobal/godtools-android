package org.cru.godtools.tutorial.layout

import com.slack.circuit.runtime.screen.ParcelablePopResult
import com.slack.circuit.runtime.screen.ParcelableScreen
import kotlinx.parcelize.Parcelize
import org.cru.godtools.tutorial.PageSet

@Parcelize
data class TutorialScreen(val pageSet: PageSet) : ParcelableScreen {
    sealed interface Result : ParcelablePopResult {
        @Parcelize data object Canceled : Result
        @Parcelize data object Finished : Result
        @Parcelize data object ShowQrCode : Result
    }
}
