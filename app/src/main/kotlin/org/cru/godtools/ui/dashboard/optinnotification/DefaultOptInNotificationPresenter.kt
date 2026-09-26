package org.cru.godtools.ui.dashboard.optinnotification

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.slack.circuit.runtime.Navigator
import com.slack.circuitx.android.IntentScreen
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import org.cru.godtools.base.CONFIG_UI_OPT_IN_NOTIFICATION_ENABLED
import org.cru.godtools.base.CONFIG_UI_OPT_IN_NOTIFICATION_PROMPT_LIMIT
import org.cru.godtools.base.CONFIG_UI_OPT_IN_NOTIFICATION_TIME_INTERVAL
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_OPT_IN_NOTIFICATION
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.cru.godtools.ui.dashboard.optinnotification.OptInNotificationPresenter.UiEvent
import org.cru.godtools.ui.dashboard.optinnotification.OptInNotificationPresenter.UiState

internal class DefaultOptInNotificationPresenter @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val controller: OptInNotificationController,
    private val remoteConfig: FirebaseRemoteConfig,
    private val settings: Settings,
) : OptInNotificationPresenter {
    @Composable
    override fun present(navigator: Navigator): UiState {
        val permissionStatus = controller.rememberPermissionStatus()
        val requestPermission = controller.rememberRequestPermission()
        var showPrompt by rememberShowPrompt(permissionStatus)

        return UiState(
            showPrompt = showPrompt,
            isHardDenied = permissionStatus == PermissionStatus.HARD_DENIED,
        ) {
            when (it) {
                UiEvent.AllowNotifications -> {
                    allowNotifications(permissionStatus, requestPermission, navigator)
                    showPrompt = false
                }

                UiEvent.Dismiss -> showPrompt = false
            }
        }
    }

    @Composable
    private fun rememberShowPrompt(permissionStatus: PermissionStatus): MutableState<Boolean> {
        val showPrompt = rememberSaveable { mutableStateOf(shouldPromptNotificationSheet(permissionStatus)) }
        var isPromptRecordPending by rememberSaveable { mutableStateOf(showPrompt.value) }
        if (isPromptRecordPending) {
            LaunchedEffect(Unit) {
                settings.recordOptInNotificationPrompt()
                isPromptRecordPending = false
            }
        }
        return showPrompt
    }

    private fun shouldPromptNotificationSheet(permissionStatus: PermissionStatus): Boolean {
        val promptLimit = remoteConfig.getLong(CONFIG_UI_OPT_IN_NOTIFICATION_PROMPT_LIMIT).toInt()
        val promptInterval = remoteConfig.getLong(CONFIG_UI_OPT_IN_NOTIFICATION_TIME_INTERVAL)

        return when {
            // TODO: Remove sdk version checks for optInNotification logic once minSdk = 33 or greater
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> false
            !remoteConfig.getBoolean(CONFIG_UI_OPT_IN_NOTIFICATION_ENABLED) -> false
            // don't prompt on the launch that shows onboarding
            !settings.isFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING) -> false
            permissionStatus == PermissionStatus.APPROVED -> false
            settings.getOptInNotificationPromptCount() > promptLimit -> false
            else -> settings.getLastPromptedOptInNotification().isBefore(LocalDate.now().minusDays(promptInterval))
        }
    }

    private fun allowNotifications(
        permissionStatus: PermissionStatus,
        requestPermission: () -> Unit,
        navigator: Navigator,
    ) {
        when (permissionStatus) {
            PermissionStatus.UNDETERMINED, PermissionStatus.SOFT_DENIED -> {
                requestPermission()
                settings.setFeatureDiscovered(FEATURE_OPT_IN_NOTIFICATION)
            }

            PermissionStatus.HARD_DENIED, PermissionStatus.APPROVED -> {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                navigator.goTo(IntentScreen(intent))
            }
        }
    }
}
