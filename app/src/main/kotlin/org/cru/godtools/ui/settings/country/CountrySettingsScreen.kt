package org.cru.godtools.ui.settings.country

import com.slack.circuit.runtime.screen.ParcelablePopResult
import com.slack.circuit.runtime.screen.ParcelableScreen
import kotlinx.parcelize.Parcelize

@Parcelize
data object CountrySettingsScreen : ParcelableScreen {
    sealed interface Result : ParcelablePopResult {
        @Parcelize data object CountrySelected : Result
        @Parcelize data object Dismissed : Result
    }
}
