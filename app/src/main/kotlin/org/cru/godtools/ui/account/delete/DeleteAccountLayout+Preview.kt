package org.cru.godtools.ui.account.delete

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.cru.godtools.base.ui.theme.GodToolsTheme

@Preview
@Composable
private fun DeleteAccountLayoutPreview() {
    GodToolsTheme {
        DeleteAccountLayout(DeleteAccountScreen.State { })
    }
}
