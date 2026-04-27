package org.cru.godtools.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.overlay.OverlayEffect
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import org.ccci.gto.android.common.compat.content.getSerializableExtraCompat
import org.cru.godtools.analytics.LaunchTrackingViewModel
import org.cru.godtools.base.EXTRA_PAGE
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.circuit.startCircuitActivity
import org.cru.godtools.base.ui.dashboard.Page
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.dashboard.optinnotification.OptInNotificationController
import org.cru.godtools.ui.dashboard.optinnotification.OptInNotificationModalOverlay
import org.cru.godtools.ui.dashboard.optinnotification.PermissionStatus
import org.cru.godtools.ui.onboarding.OnboardingScreen
import org.cru.godtools.util.openToolActivity

@AndroidEntryPoint
class DashboardActivity : BaseActivity() {
    private val viewModel: DashboardViewModel by viewModels()
    private val launchTrackingViewModel: LaunchTrackingViewModel by viewModels()

    @Inject
    lateinit var remoteConfig: FirebaseRemoteConfig
    private val optInNotificationController by lazy {
        OptInNotificationController(this, viewModel, remoteConfig, settings)
    }

    lateinit var permissionLauncher: ActivityResultLauncher<String>
    var permissionContinuation: Continuation<Boolean>? = null

    @Inject
    lateinit var circuit: Circuit

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) intent?.let { processIntent(it) }
        triggerOnboardingIfNecessary()

        // region optInNotification
        optInNotificationController.init()

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            permissionContinuation?.resume(granted)
            permissionContinuation = null
        }

        optInNotificationController.shouldPromptNotificationSheet()
        // endregion optInNotification

        enableEdgeToEdge()
        setContent {
            CircuitCompositionLocals(circuit) {
                GodToolsTheme {
                    ContentWithOverlays {
                        // region optInNotification
                        val showOverlay by viewModel.showOptInNotification.collectAsState()

                        if (showOverlay) {
                            OverlayEffect {
                                val overlay = OptInNotificationModalOverlay(
                                    requestPermission = { optInNotificationController.requestNotificationPermission() },
                                    isHardDenied = viewModel.permissionStatus == PermissionStatus.HARD_DENIED
                                )
                                show(overlay)
                                viewModel.setShowOptInNotification(false)
                            }
                        }
                        // endregion optInNotification

                        DashboardLayout(
                            onEvent = { e ->
                                when (e) {
                                    is DashboardEvent.OpenIntent -> startActivity(e.intent)
                                    is DashboardEvent.OpenScreen -> startCircuitActivity(e.screen)
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
        optInNotificationController.onResume()
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
                    data == null -> Unit

                    data.isDashboardCustomUriSchemeDeepLink() ->
                        viewModel.updateCurrentPage(findPageByUriPathSegment(data.pathSegments.getOrNull(1)))

                    data.isDashboardDynalinksDeepLink() || data.isDashboardGodToolsDeepLink() ->
                        viewModel.updateCurrentPage(findPageByUriPathSegment(data.pathSegments.getOrNull(2)))

                    data.isDashboardLessonsDeepLink() -> viewModel.updateCurrentPage(Page.LESSONS)
                }
            }
        }
    }
    // endregion Intent processing

    private fun triggerOnboardingIfNecessary() {
        if (settings.isFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING)) return
        optInNotificationController.isOnboardingLaunch = true
        startCircuitActivity(OnboardingScreen)
    }

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
