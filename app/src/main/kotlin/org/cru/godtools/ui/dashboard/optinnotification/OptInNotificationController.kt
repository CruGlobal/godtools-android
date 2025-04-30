package org.cru.godtools.ui.dashboard.optinnotification

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import java.util.Calendar
import java.util.Date
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import org.cru.godtools.base.CONFIG_UI_OPT_IN_NOTIFICATION_ENABLED
import org.cru.godtools.base.CONFIG_UI_OPT_IN_NOTIFICATION_PROMPT_LIMIT
import org.cru.godtools.base.CONFIG_UI_OPT_IN_NOTIFICATION_TIME_INTERVAL
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_OPT_IN_NOTIFICATION
import org.cru.godtools.ui.dashboard.DashboardActivity
import org.cru.godtools.ui.dashboard.DashboardViewModel

class OptInNotificationController(
    private val activity: DashboardActivity,
    private val viewModel: DashboardViewModel,
    private val remoteConfig: FirebaseRemoteConfig,
    private val settings: Settings,
) {
    var isOnboardingLaunch = false

    fun init() {
        val permissionStatus = checkNotificationPermissionStatus()
        viewModel.setPermissionStatus(permissionStatus)
    }

    fun onResume() {
        val permissionStatus = checkNotificationPermissionStatus()
        viewModel.setPermissionStatus(permissionStatus)
    }

    private fun checkNotificationPermissionStatus(): PermissionStatus {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                !settings.isFeatureDiscovered(FEATURE_OPT_IN_NOTIFICATION) -> {
                    return PermissionStatus.UNDETERMINED
                }

                ContextCompat.checkSelfPermission(
                    activity, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    return PermissionStatus.APPROVED
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.POST_NOTIFICATIONS
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

    private fun openNotificationSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }

    fun shouldPromptNotificationSheet() {

        val lastPrompted = settings.getLastPromptedOptInNotification() ?: Date(Long.MIN_VALUE)
        val promptCount = settings.getOptInNotificationPromptCount()

        val remoteTimeIntervalLong = remoteConfig.getLong(CONFIG_UI_OPT_IN_NOTIFICATION_TIME_INTERVAL)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -remoteTimeIntervalLong.toInt())
        val remoteTimeInterval = calendar.time

        val remotePromptLimit = remoteConfig.getLong(CONFIG_UI_OPT_IN_NOTIFICATION_PROMPT_LIMIT).toInt()

        val remoteFeatureEnabled = remoteConfig.getBoolean(CONFIG_UI_OPT_IN_NOTIFICATION_ENABLED)

        // TODO: Remove sdk version checks for optInNotification logic once minSdk = 33 or greater
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || !remoteFeatureEnabled || isOnboardingLaunch || viewModel.permissionStatus == PermissionStatus.APPROVED || promptCount > remotePromptLimit) {
            return
        }

        if (lastPrompted < remoteTimeInterval) {
            viewModel.setShowOptInNotification(true)
            settings.recordOptInNotificationPrompt()
        }
    }

    suspend fun requestNotificationPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        activity.permissionContinuation = continuation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (viewModel.permissionStatus == PermissionStatus.UNDETERMINED || viewModel.permissionStatus == PermissionStatus.SOFT_DENIED) {
                activity.permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                settings.setFeatureDiscovered(FEATURE_OPT_IN_NOTIFICATION)
            } else {
                openNotificationSettings()
                continuation.resume(true)
                activity.permissionContinuation = null
            }
        } else {
            continuation.resume(true)
            activity.permissionContinuation = null
        }
    }
}
