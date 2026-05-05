package org.cru.godtools.ui.settings.country

import com.slack.circuit.runtime.screen.PopResult
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data object CountrySettingsScreen : Screen {
    sealed interface Result : PopResult {
        @Parcelize data object CountrySelected : Result
        @Parcelize data object Dismissed : Result
    }
}
