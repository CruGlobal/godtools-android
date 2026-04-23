package org.cru.godtools.ui.banner

import androidx.compose.runtime.Composable

interface BannerPresenter<T : Banner.UiState> {
    @Composable
    fun present(): T?
}
