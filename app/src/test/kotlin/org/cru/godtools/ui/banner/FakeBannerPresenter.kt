package org.cru.godtools.ui.banner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow

class FakeBannerPresenter<T : Banner.UiState>(initialState: T? = null) : BannerPresenter<T> {
    private val uiState = MutableStateFlow(initialState)

    fun updateState(state: T?) {
        uiState.value = state
    }

    @Composable
    override fun present(): T? = uiState.collectAsState().value
}
