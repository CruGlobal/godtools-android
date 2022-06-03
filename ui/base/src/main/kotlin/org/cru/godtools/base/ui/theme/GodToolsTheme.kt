package org.cru.godtools.base.ui.theme

import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import org.cru.godtools.base.ui.util.openUrl

private val GT_BLUE = Color(red = 0x3B, green = 0xA4, blue = 0xDB)

private val GodToolsLightColorScheme = lightColorScheme(primary = GT_BLUE)

@Composable
fun GodToolsTheme(content: @Composable () -> Unit) {
    ProvideCompositionLocals {
        MaterialTheme(
            colorScheme = GodToolsLightColorScheme,
            content = content
        )
    }
}

@Composable
private fun ProvideCompositionLocals(content: @Composable () -> Unit) {
    val context = LocalContext.current

    val uriHandler = remember {
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
