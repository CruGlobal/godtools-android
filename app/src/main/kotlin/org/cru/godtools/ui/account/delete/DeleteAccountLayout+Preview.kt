package org.cru.godtools.ui.account.delete

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.ui.account.delete.DeleteAccountPresenter.UiState

@Preview
@Composable
private fun DeleteAccountLayoutDisplayPreview() {
    GodToolsTheme { DeleteAccountLayout(UiState.Display()) }
}

@Preview
@Composable
private fun DeleteAccountLayoutDeletingPreview() {
    GodToolsTheme { DeleteAccountLayout(UiState.Deleting()) }
}

@Preview
@Composable
private fun DeleteAccountLayoutErrorPreview() {
    GodToolsTheme { DeleteAccountLayout(UiState.Error()) }
}
