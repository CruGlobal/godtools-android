package org.cru.godtools.ui.dashboard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.ktx.AppUpdateResult
import com.google.android.play.core.ktx.clientVersionStalenessDays
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.ktx.requestUpdateFlow
import kotlinx.coroutines.flow.catch
import org.cru.godtools.R

internal val LocalAppUpdateManager = staticCompositionLocalOf<AppUpdateManager?> { null }

@Composable
internal fun AppUpdateSnackbar(hostState: SnackbarHostState) {
    val updateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {}
    )

    val context = LocalContext.current
    val appUpdateManager = LocalAppUpdateManager.current ?: remember { AppUpdateManagerFactory.create(context) }
    val appUpdateResult = remember(appUpdateManager) {
        appUpdateManager.requestUpdateFlow()
            .catch { emit(AppUpdateResult.NotAvailable) }
    }.collectAsState(AppUpdateResult.NotAvailable).value

    LaunchedEffect(appUpdateResult) {
        when (appUpdateResult) {
            is AppUpdateResult.Available -> {
                val updateInfo = appUpdateResult.updateInfo
                if ((updateInfo.clientVersionStalenessDays ?: Int.MIN_VALUE) < 14) return@LaunchedEffect

                val result = hostState.showSnackbar(
                    context.getString(R.string.play_update_available),
                    actionLabel = context.getString(R.string.play_update_available_action),
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite
                )
                if (result == SnackbarResult.Dismissed) return@LaunchedEffect

                if (updateInfo.isFlexibleUpdateAllowed) {
                    appUpdateResult.startFlexibleUpdate(updateLauncher)
                } else if (updateInfo.isImmediateUpdateAllowed) {
                    appUpdateResult.startImmediateUpdate(updateLauncher)
                }
            }

            is AppUpdateResult.Downloaded -> {
                val result = hostState.showSnackbar(
                    context.getString(R.string.play_update_downloaded),
                    actionLabel = context.getString(R.string.play_update_downloaded_action),
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite
                )
                if (result == SnackbarResult.Dismissed) return@LaunchedEffect

                appUpdateResult.completeUpdate()
            }

            else -> Unit
        }
    }
}
