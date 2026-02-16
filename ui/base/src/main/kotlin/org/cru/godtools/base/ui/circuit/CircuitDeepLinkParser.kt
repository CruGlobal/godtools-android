package org.cru.godtools.base.ui.circuit

import android.net.Uri
import com.slack.circuit.runtime.screen.Screen

interface CircuitDeepLinkParser {
    fun isDeepLinkSupported(uri: Uri): Boolean
    fun parseDeepLink(uri: Uri): List<Screen>
}
