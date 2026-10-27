package org.cru.godtools.base.ui.circuit.screen

import com.slack.circuit.runtime.screen.ParcelablePopResult
import com.slack.circuit.runtime.screen.ParcelableScreen
import kotlinx.parcelize.Parcelize

@Parcelize
data object AppLanguageScreen : ParcelableScreen {
    sealed interface Result : ParcelablePopResult {
        @Parcelize data object LanguageSelected : Result
        @Parcelize data object Dismissed : Result
    }
}
