package org.cru.godtools.tool.tract.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import org.cru.godtools.shared.renderer.state.State
import org.cru.godtools.shared.renderer.tract.RenderTractModal
import org.cru.godtools.shared.tool.parser.model.tract.Modal

internal class ModalOverlay(private val modal: Modal, private val state: State) : Overlay<Unit> {
    @Composable
    override fun Content(navigator: OverlayNavigator<Unit>) {
        RenderTractModal(
            modal,
            state,
            onDismiss = { navigator.finish(Unit) },
            modifier = Modifier.fillMaxSize()
        )
    }
}
