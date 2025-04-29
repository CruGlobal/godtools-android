package org.cru.godtools.ui.dashboard.optinnotification

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
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
                    println("status via controller: Undetermined")
                    return PermissionStatus.UNDETERMINED}

                ContextCompat.checkSelfPermission(
                    activity, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    println("status via controller: Approved")
                    return PermissionStatus.APPROVED}

                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    println("status via controller: Soft Denied")
                    return PermissionStatus.SOFT_DENIED}

                else -> {
                    println("status via controller: Hard Denied")
                    return PermissionStatus.HARD_DENIED}
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

        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val lastPromptedTestDate = dateFormat.parse("01/01/2020") ?: Date()

        val remoteTimeIntervalString = remoteConfig.getLong("ui_opt_in_notification_time_interval")
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -remoteTimeIntervalString.toInt())
        val remoteTimeInterval = calendar.time

        val remotePromptLimit = remoteConfig.getLong("ui_opt_in_notification_prompt_limit").toInt()
        println("Got time interval: $remoteTimeIntervalString - Got prompt limit: $remotePromptLimit")

        // TODO: Remove sdk version checks for optInNotification logic once minSdk = 33 or greater
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            println("Returning due to ineligible SDK version")
            return}
        if (isOnboardingLaunch) {
            println("Returning due to onboarding launch")
            return}
        if (viewModel.permissionStatus == PermissionStatus.APPROVED) {
            println("Returning due to approved permission status")
            return}
        if (promptCount > remotePromptLimit) {
            println("Returning due to prompt count")
            return}

        // return if isOnboardingLaunch, notification permission is already granted, or promptCount exceeds maxPrompts

        if (lastPromptedTestDate < remoteTimeInterval) {
            viewModel.setShowOptInNotification(true)
            settings.recordOptInNotificationPrompt()
        }
    }

    suspend fun requestNotificationPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        activity.permissionContinuation = continuation

        if (viewModel.permissionStatus == PermissionStatus.UNDETERMINED || viewModel.permissionStatus == PermissionStatus.SOFT_DENIED) {
            activity.permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            settings.setFeatureDiscovered(FEATURE_OPT_IN_NOTIFICATION)
        } else {
            openNotificationSettings()
            continuation.resume(true)
            activity.permissionContinuation = null
        }
    }


}
