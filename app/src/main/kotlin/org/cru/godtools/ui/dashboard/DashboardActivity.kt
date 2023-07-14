package org.cru.godtools.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.asLiveData
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import org.ccci.gto.android.common.compat.content.getSerializableExtraCompat
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.analytics.LaunchTrackingViewModel
import org.cru.godtools.base.EXTRA_PAGE
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.ui.dashboard.Page
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.databinding.ActivityDashboardBinding
import org.cru.godtools.model.Tool
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.startTutorialActivity
import org.cru.godtools.ui.tooldetails.startToolDetailsActivity
import org.cru.godtools.util.openToolActivity

private const val TAG_PARALLEL_LANGUAGE_DIALOG = "parallelLanguageDialog"

@AndroidEntryPoint
class DashboardActivity : BasePlatformActivity<ActivityDashboardBinding>() {
    private val viewModel: DashboardViewModel by viewModels()
    private val launchTrackingViewModel: LaunchTrackingViewModel by viewModels()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) intent?.let { processIntent(it) }
        triggerOnboardingIfNecessary()
    }

    override fun onBindingChanged() {
        super.onBindingChanged()
        binding.compose.setContent {
            GodToolsTheme {
                DashboardLayout(
                    onOpenTool = { t, lang1, lang2 -> openTool(t, *listOfNotNull(lang1, lang2).toTypedArray()) },
                    onOpenToolDetails = { startToolDetailsActivity(it) }
                )
            }
        }
    }

    override fun onSetupActionBar() {
        super.onSetupActionBar()
        title = ""
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)
        processIntent(newIntent)
    }

    override fun onResume() {
        super.onResume()
        launchTrackingViewModel.trackLaunch()
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
                    data?.isDashboardCustomUriSchemeDeepLink() == true ->
                        viewModel.updateCurrentPage(findPageByUriPathSegment(data.pathSegments.getOrNull(1)))
                    data?.isDashboardGodToolsDeepLink() == true ->
                        viewModel.updateCurrentPage(findPageByUriPathSegment(data.pathSegments.getOrNull(2)))
                    data?.isDashboardLessonsDeepLink() == true -> viewModel.updateCurrentPage(Page.LESSONS)
                }
            }
        }
    }
    // endregion Intent processing

    private fun triggerOnboardingIfNecessary() {
        if (settings.isFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING)) return
        startTutorialActivity(PageSet.ONBOARDING)
    }

    // region UI
    override val toolbar get() = binding.appbar
    override val drawerLayout get() = binding.drawerLayout
    override val drawerMenu get() = binding.drawerMenu

    override val isShowNavigationDrawerIndicator by lazy {
        viewModel.currentPage.map { it != Page.FAVORITE_TOOLS }.asLiveData()
    }

    override fun inflateBinding() = ActivityDashboardBinding.inflate(layoutInflater)

    // region ToolsAdapterCallbacks
    @Inject
    internal lateinit var lazyManifestManager: Lazy<ManifestManager>
    private val manifestManager get() = lazyManifestManager.get()

    private fun openTool(tool: Tool?, vararg languages: Locale) {
        val code = tool?.code ?: return
        if (languages.isEmpty()) return

        languages.forEach { manifestManager.preloadLatestPublishedManifest(code, it) }
        openToolActivity(code, tool.type, *languages)
    }
    // endregion ToolsAdapterCallbacks
    // endregion UI

    override fun onSupportNavigateUp() = when {
        onBackPressedDispatcher.hasEnabledCallbacks() -> {
            onBackPressedDispatcher.onBackPressed()
            true
        }
        else -> super.onSupportNavigateUp()
    }
}
