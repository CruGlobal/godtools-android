package org.cru.godtools.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.overlay.ContentWithOverlays
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import org.ccci.gto.android.common.compat.content.getSerializableExtraCompat
import org.cru.godtools.analytics.LaunchTrackingViewModel
import org.cru.godtools.base.EXTRA_PAGE
import org.cru.godtools.base.Settings.Companion.FEATURE_OPT_IN_NOTIFICATION
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.dashboard.Page
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.model.Tool
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.startTutorialActivity
import org.cru.godtools.ui.tooldetails.startToolDetailsActivity
import org.cru.godtools.util.openToolActivity

enum class PermissionStatus {
    APPROVED, // Approved
    SOFT_DENIED,  // Denied but requestable
    HARD_DENIED, // Denied and no longer requestable
    UNDETERMINED // First time request
}
@AndroidEntryPoint
class DashboardActivity : BaseActivity() {
    private val viewModel: DashboardViewModel by viewModels()
    private val launchTrackingViewModel: LaunchTrackingViewModel by viewModels()

    //here
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var permissionContinuation: Continuation<Boolean>? = null

    @Inject
    lateinit var circuit: Circuit

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) intent?.let { processIntent(it) }
        triggerOnboardingIfNecessary()

        //here
        checkNotificationPermissionStatus()
        viewModel.shouldPromptNotificationSheet()
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            permissionContinuation?.resume(granted)
            permissionContinuation = null
        }
        enableEdgeToEdge()
        setContent {
            CircuitCompositionLocals(circuit) {
                GodToolsTheme {
                    ContentWithOverlays {
                        DashboardLayout(
                            requestPermission = { requestNotificationPermission() },
                            openNotificationSettings = { openNotificationSettings() },
                            onEvent = { e ->
                                when (e) {
                                    is DashboardEvent.OpenIntent -> startActivity(e.intent)
                                    is DashboardEvent.OpenTool -> openTool(
                                        e.tool, e.type, *listOfNotNull(e.lang1, e.lang2).toTypedArray()
                                    )

                                    is DashboardEvent.OpenToolDetails -> e.tool?.let {
                                        startToolDetailsActivity(
                                            it, e.lang
                                        )
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)
        processIntent(newIntent)
    }

    override fun onResume() {
        super.onResume()
        launchTrackingViewModel.trackLaunch()
        println("onResume triggered")
        checkNotificationPermissionStatus()
    }
    // endregion Lifecycle

    // region Intent processing
    private fun processIntent(intent: Intent) {
        val page = intent.getSerializableExtraCompat(EXTRA_PAGE, Page::class.java)
        when {
            page != null -> viewModel.updateCurrentPage(page)
            intent.action == Intent.ACTION_VIEW -> {
                val data = intent.data
                when {
                    data?.isDashboardCustomUriSchemeDeepLink() == true -> viewModel.updateCurrentPage(
                        findPageByUriPathSegment(data.pathSegments.getOrNull(1))
                    )

                    data?.isDashboardGodToolsDeepLink() == true -> viewModel.updateCurrentPage(
                        findPageByUriPathSegment(
                            data.pathSegments.getOrNull(2)
                        )
                    )

                    data?.isDashboardLessonsDeepLink() == true -> viewModel.updateCurrentPage(Page.LESSONS)
                }
            }
        }
    }
    // endregion Intent processing

    private fun triggerOnboardingIfNecessary() {
        if (settings.isFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING)) return
        viewModel.isOnboardingLaunch = true
        startTutorialActivity(PageSet.ONBOARDING)
    }

    // region optInNotification

    private fun checkNotificationPermissionStatus() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                !settings.isFeatureDiscovered(FEATURE_OPT_IN_NOTIFICATION) -> {
                    viewModel.setPermissionStatus(PermissionStatus.UNDETERMINED)
                    println("Permission Status: Undetermined")
                }

                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    viewModel.setPermissionStatus(PermissionStatus.APPROVED)
                    println("Permission Status: Approved")
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    viewModel.setPermissionStatus(PermissionStatus.SOFT_DENIED)
                    println("Permission Status: Soft Denied")
                }

                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_DENIED -> {
                    viewModel.setPermissionStatus(PermissionStatus.HARD_DENIED)
                    println("Permission Status: Hard Denied")
                }
            }
        }
    }

    private suspend fun requestNotificationPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        permissionContinuation = continuation

        if (viewModel.permissionStatus.value == PermissionStatus.UNDETERMINED || viewModel.permissionStatus.value == PermissionStatus.SOFT_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
                settings.setFeatureDiscovered(FEATURE_OPT_IN_NOTIFICATION)
            } else {
                continuation.resume(true)
                permissionContinuation = null
            }
        } else {
                // TODO - DSR: await the result of settings dialog before dismissing bottom sheet
            viewModel.setShowNotificationSettingsDialog(true)

            continuation.resume(true)
            permissionContinuation = null
        }
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }


    // endregion optInNotification

    // region ToolsAdapterCallbacks
    @Inject
    internal lateinit var lazyManifestManager: Lazy<ManifestManager>
    private val manifestManager get() = lazyManifestManager.get()

    private fun openTool(tool: String?, type: Tool.Type?, vararg languages: Locale) {
        if (tool == null || type == null) return
        if (languages.isEmpty()) return

        languages.forEach { manifestManager.preloadLatestPublishedManifest(tool, it) }
        openToolActivity(tool, type, *languages)
    }
    // endregion ToolsAdapterCallbacks
    // endregion UI
}
