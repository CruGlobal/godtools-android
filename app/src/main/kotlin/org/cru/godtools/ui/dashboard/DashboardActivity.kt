package org.cru.godtools.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.overlay.OverlayEffect
import com.slack.circuit.runtime.screen.ParcelableScreen
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.android.IntentScreen
import com.slack.circuitx.navigation.intercepting.InterceptedResult
import com.slack.circuitx.navigation.intercepting.NavigationContext
import com.slack.circuitx.navigation.intercepting.NavigationInterceptor
import com.slack.circuitx.navigation.intercepting.NavigationInterceptor.Companion.Skipped
import com.slack.circuitx.navigation.intercepting.NavigationInterceptor.Companion.SuccessConsumed
import com.slack.circuitx.navigation.intercepting.rememberInterceptingNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import org.cru.godtools.analytics.LaunchTrackingViewModel
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.circuit.screen.dashboard.DashboardScreen
import org.cru.godtools.base.ui.circuit.startCircuitActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.ui.dashboard.optinnotification.OptInNotificationController
import org.cru.godtools.ui.dashboard.optinnotification.OptInNotificationModalOverlay
import org.cru.godtools.ui.dashboard.optinnotification.PermissionStatus
import org.cru.godtools.ui.onboarding.OnboardingScreen
import timber.log.Timber

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

    // region Circuit
    @Inject
    lateinit var circuit: Circuit
    private val deepLinkNavEvents = Channel<NavEvent>(Channel.UNLIMITED)

    private val navigationInterceptor = object : NavigationInterceptor {
        override fun goTo(screen: Screen, navigationContext: NavigationContext): InterceptedResult {
            @Suppress("ktlint:standard:blank-line-between-when-conditions")
            when (screen) {
                is IntentScreen -> screen.startWith(this@DashboardActivity)
                is ParcelableScreen -> startCircuitActivity(screen)
                else -> Timber.tag("DashboardActivity")
                    .e(IllegalArgumentException("Unhandled Circuit Screen in DashboardActivity: $screen"))
            }
            return SuccessConsumed
        }
    }
    // endregion Circuit

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialScreen = intent?.let { processIntent(it) } ?: DashboardScreen()
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

                        val backStack = rememberSaveableBackStack(initialScreen)
                        val navigator = rememberInterceptingNavigator(
                            rememberCircuitNavigator(backStack),
                            interceptors = remember { listOf(navigationInterceptor) }
                        )

                        LaunchedEffect(deepLinkNavEvents) {
                            deepLinkNavEvents.consumeEach { navigator.onNavEvent(it) }
                        }

                        NavigableCircuitContent(
                            navigator,
                            backStack,
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)
        val screen = processIntent(newIntent)
        if (screen != null) {
            deepLinkNavEvents.trySend(NavEvent.ResetRoot(screen))
        }
    }

    override fun onResume() {
        super.onResume()
        launchTrackingViewModel.trackLaunch()
        optInNotificationController.onResume()
    }
    // endregion Lifecycle

    // region Intent processing
    private fun processIntent(intent: Intent): DashboardScreen? {
        val uri = intent.data
        if (intent.action == Intent.ACTION_VIEW && uri != null && DashboardDeepLinkParser.isDeepLinkSupported(uri)) {
            return DashboardDeepLinkParser.parseDeepLink(uri)
                .filterIsInstance<DashboardScreen>()
                .firstOrNull()
        }

        return null
    }
    // endregion Intent processing

    private fun triggerOnboardingIfNecessary() {
        if (settings.isFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING)) return
        optInNotificationController.isOnboardingLaunch = true
        startCircuitActivity(OnboardingScreen)
    }
}
