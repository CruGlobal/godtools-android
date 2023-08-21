package org.cru.godtools.downloadmanager.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import org.cru.godtools.downloadmanager.GodToolsDownloadManager
import org.cru.godtools.model.TranslationKey

@Composable
fun DownloadLatestTranslation(tool: String?, locale: Locale?) {
    if (LocalInspectionMode.current) return
    if (tool == null || locale == null) return

    val context = LocalContext.current
    val dagger = remember(context) { EntryPointAccessors.fromApplication<DownloadLatestTranslationEntryPoint>(context) }

    LaunchedEffect(tool, locale) {
        dagger.downloadManager.downloadLatestPublishedTranslation(TranslationKey(tool, locale))
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface DownloadLatestTranslationEntryPoint {
    val downloadManager: GodToolsDownloadManager
}
