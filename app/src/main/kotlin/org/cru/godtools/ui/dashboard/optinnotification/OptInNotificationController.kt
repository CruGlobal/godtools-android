package org.cru.godtools.ui.dashboard.optinnotification

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import javax.inject.Inject
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_OPT_IN_NOTIFICATION

internal interface OptInNotificationController {
    @Composable
    fun rememberPermissionStatus(): PermissionStatus

    @Composable
    fun rememberRequestPermission(): () -> Unit
}

internal class DefaultOptInNotificationController @Inject constructor(private val settings: Settings) :
    OptInNotificationController {
    @Composable
    override fun rememberPermissionStatus(): PermissionStatus {
        val activity = LocalActivity.current ?: return PermissionStatus.APPROVED

        var permissionStatus by remember(activity) { mutableStateOf(checkNotificationPermissionStatus(activity)) }
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
            permissionStatus = checkNotificationPermissionStatus(activity)
        }
        return permissionStatus
    }

    @Composable
    override fun rememberRequestPermission(): () -> Unit {
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), onResult = {})

        return remember(launcher) {
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun checkNotificationPermissionStatus(activity: Activity): PermissionStatus {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                !settings.isFeatureDiscovered(FEATURE_OPT_IN_NOTIFICATION) -> {
                    return PermissionStatus.UNDETERMINED
                }

                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    return PermissionStatus.APPROVED
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    return PermissionStatus.SOFT_DENIED
                }

                else -> {
                    return PermissionStatus.HARD_DENIED
                }
            }
        } else {
            PermissionStatus.APPROVED
        }
    }
}
