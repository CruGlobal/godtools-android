package org.cru.godtools.base.ui.compose

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import org.cru.godtools.base.ui.util.openUrl

@Composable
internal fun CompositionLocals(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val uriHandler = remember(context) {
        object : UriHandler {
            override fun openUri(uri: String) {
                context.openUrl(Uri.parse(uri))
            }
        }
    }

    CompositionLocalProvider(
        LocalUriHandler provides uriHandler,
        content = content
    )
}
