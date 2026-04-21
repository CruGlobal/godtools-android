package org.cru.godtools.base.ui.circuit.screen

import com.slack.circuit.runtime.screen.PopResult
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data object AppLanguageScreen : Screen {
    sealed interface Result : PopResult {
        @Parcelize data object SetLanguage : Result
        @Parcelize data object DismissAppLanguage : Result
    }
}
