package org.cru.godtools.tool.lesson.ui

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.overlay.OverlayEffect
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.lifecycle.SetLiveData
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.getMutableStateFlow
import org.ccci.gto.android.common.androidx.viewpager2.widget.whileMaintainingVisibleCurrentItem
import org.cru.godtools.base.CONFIG_TUTORIAL_LESSON_PAGE_SWIPE
import org.cru.godtools.base.HOST_DYNALINKS
import org.cru.godtools.base.HOST_GODTOOLSAPP_COM
import org.cru.godtools.base.SCHEME_GODTOOLS
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_LESSON_FEEDBACK
import org.cru.godtools.base.Settings.Companion.FEATURE_LESSON_PAGE_SWIPED
import org.cru.godtools.base.tool.EXTRA_RESUME_PAGE
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity
import org.cru.godtools.base.tool.activity.BaseSingleToolActivityDataModel
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.downloadmanager.GodToolsDownloadManager
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.lesson.LessonPage
import org.cru.godtools.tool.lesson.BuildConfig.HOST_GODTOOLS_CUSTOM_URI
import org.cru.godtools.tool.lesson.R
import org.cru.godtools.tool.lesson.analytics.model.LessonPageAnalyticsScreenEvent
import org.cru.godtools.tool.lesson.databinding.LessonActivityBinding
import org.cru.godtools.tool.lesson.ui.feedback.LessonFeedbackDialogFragment
import org.cru.godtools.tool.lesson.ui.resume.LessonResumeDialogFragment
import org.cru.godtools.tool.lesson.ui.swipetutorial.LessonSwipeTutorialAnimatedModalOverlay
import org.cru.godtools.tool.lesson.util.isLessonDeepLink
import org.cru.godtools.user.activity.UserActivityManager

private const val TAG_RESUME_DIALOG = "resume_dialog"

@AndroidEntryPoint
class LessonActivity :
    BaseSingleToolActivity<LessonActivityBinding>(
        contentLayoutId = R.layout.lesson_activity,
        requireTool = true,
        supportedType = Manifest.Type.LESSON
    ),
    LessonPageAdapter.Callbacks {

    @Inject
    internal lateinit var toolsRepository: ToolsRepository

    override val viewModel: LessonActivityDataModel by viewModels()
    override val dataModel get() = viewModel

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        if (savedInstanceState == null) trackToolOpen(tool, Manifest.Type.LESSON)
        setupResumeDialog()
        setupFeedbackDialog()
    }

    override fun onBindingChanged() {
        super.onBindingChanged()
        binding.setupPages()
        binding.setupComposeOverlays()
        setupProgressTracking()
        binding.trackPageSwipedFeatureDiscovery()
    }

    override fun onResume() {
        super.onResume()
        trackPageInAnalytics()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> showFeedbackDialogIfNecessary() || super.onOptionsItemSelected(item)
        else -> super.onOptionsItemSelected(item)
    }

    override fun onContentEvent(event: Event) {
        checkForPageEvent(event)
    }
    // endregion Lifecycle

    // region Intent Processing
    override fun processIntent(intent: Intent, savedInstanceState: Bundle?) {
        super.processIntent(intent, savedInstanceState)
        val data = intent.data?.normalizeScheme() ?: return
        val path = data.pathSegments ?: return

        when (intent.action) {
            ACTION_VIEW -> when {
                data.isDynalinksDeepLink() || data.isGodToolsDeepLink() -> {
                    dataModel.toolCode.value = path[3]
                    dataModel.locale.value = Locale.forLanguageTag(path[4])
                }
                // Sample deep link: godtools://org.cru.godtools/tool/lesson/{tool}/{locale}
                data.isCustomUriDeepLink() -> {
                    dataModel.toolCode.value = path[2]
                    dataModel.locale.value = Locale.forLanguageTag(path[3])
                }
                // Sample Lesson deep link: https://godtoolsapp.com/lessons/lessonholyspirit/en
                data.isLessonDeepLink() -> {
                    dataModel.toolCode.value = path[1]
                    dataModel.locale.value = Locale.forLanguageTag(path[2])
                }
            }
        }
    }

    private fun Uri.isDynalinksDeepLink() = ("http".equals(scheme, true) || "https".equals(scheme, true)) &&
        HOST_DYNALINKS.equals(host, true) &&
        pathSegments.orEmpty().size >= 5 &&
        path?.startsWith("/deeplink/tool/lesson/") == true

    private fun Uri.isGodToolsDeepLink() = ("http".equals(scheme, true) || "https".equals(scheme, true)) &&
        HOST_GODTOOLSAPP_COM.equals(host, true) &&
        pathSegments.orEmpty().size >= 5 &&
        path?.startsWith("/deeplink/tool/lesson/") == true

    private fun Uri.isCustomUriDeepLink() = scheme == SCHEME_GODTOOLS &&
        HOST_GODTOOLS_CUSTOM_URI.equals(host, true) &&
        pathSegments.orEmpty().size >= 4 &&
        pathSegments?.getOrNull(0) == "tool" &&
        pathSegments?.getOrNull(1) == "lesson"
    // endregion Intent Processing

    // region UI
    override val toolbar get() = binding.appbar

    // region Progress
    private fun setupProgressTracking() {
        dataModel.pages.observe(this@LessonActivity) { updateProgress(pages = it) }
    }

    private fun updateProgress(
        position: Int = binding.pages.currentItem,
        pages: List<LessonPage>? = dataModel.pages.value
    ) {
        // update progress in database unless we are waiting for the user to resume/restart
        if (resumePageId == null) {
            lifecycleScope.launch {
                toolsRepository.updateToolProgress(
                    tool,
                    if (pages.isNullOrEmpty()) 0.0 else (position.toDouble() / pages.size),
                    pages?.getOrNull(position)?.id
                )
            }
        }

        // update progress indicator
        val max = pages?.count { !it.isHidden } ?: 0
        val progress = pages?.take(position + 1)?.count { !it.isHidden }?.coerceAtMost(max) ?: 0

        binding.progress.max = max
        // TODO: switch to setProgressCompat(p, true) once this bug is fixed:
        //       https://github.com/material-components/material-components-android/issues/2051
        binding.progress.progress = progress
    }
    // endregion Progress

    // region Pages
    @Inject
    lateinit var lessonPageAdapterFactory: LessonPageAdapter.Factory
    private val lessonPageAdapter by lazy { lessonPageAdapterFactory.create(this, this, toolState.toolState) }

    private fun LessonActivityBinding.setupPages() {
        pages.adapter = lessonPageAdapter
        pages.offscreenPageLimit = 1
        dataModel.pages.observe(this@LessonActivity) { lessonPages ->
            pages.whileMaintainingVisibleCurrentItem { lessonPageAdapter.pages = lessonPages }
        }

        pages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == SCROLL_STATE_IDLE) {
                    // HACK: execute this on the next frame to avoid updating the visible pages during a scroll callback
                    lifecycleScope.launch(Dispatchers.Main) {
                        // remove any visible pages that are no longer the active page
                        val currentItemId = dataModel.pages.value?.getOrNull(pages.currentItem)?.id
                        dataModel.visiblePages.removeAll { it != currentItemId }
                    }
                }
            }

            override fun onPageSelected(position: Int) {
                updateProgress(position = position)
                trackPageInAnalytics(dataModel.pages.value?.getOrNull(position))
                dataModel.pageReached.value = maxOf(position, dataModel.pageReached.value)
            }
        })
    }

    private fun checkForPageEvent(event: Event) {
        val page = dataModel.manifest.value?.pages?.firstOrNull { it.listeners.contains(event.id) }
        if (page != null) {
            dataModel.visiblePages += page.id
            dataModel.pages.value?.indexOfFirst { it.id == page.id }?.takeIf { it >= 0 }?.let {
                binding.pages.currentItem = it
            }
        }
    }

    override fun goToPreviousPage() {
        binding.pages.currentItem -= 1
    }

    override fun goToNextPage() {
        binding.pages.currentItem += 1
    }
    // endregion Pages

    // region Resume Progress
    private var resumePageId: String?
        get() = intent?.getStringExtra(EXTRA_RESUME_PAGE)
        set(value) {
            intent?.putExtra(EXTRA_RESUME_PAGE, value)
        }

    private fun indexOfResumePage(): Int {
        val pageId = resumePageId ?: return -1
        val pages = dataModel.pages.value?.takeIf { it.isNotEmpty() } ?: return -1

        return dataModel.manifest.value?.findPage(pageId)
            ?.let { generateSequence(it) { it.previousPage }.firstOrNull { !it.isHidden } }
            ?.let { pages.indexOf(it) } ?: -1
    }

    private fun setupResumeDialog() {
        supportFragmentManager.setFragmentResultListener(LessonResumeDialogFragment.RESULT_RESUME, this) { _, _ ->
            indexOfResumePage().takeIf { it >= 0 }?.let { binding.pages.currentItem = it }
            resumePageId = null
            updateProgress()
        }
        supportFragmentManager.setFragmentResultListener(LessonResumeDialogFragment.RESULT_RESTART, this) { _, _ ->
            resumePageId = null
            updateProgress()
        }

        dataModel.pages.observe(this) { triggerResumeProgress() }
    }

    private fun triggerResumeProgress() {
        if (supportFragmentManager.findFragmentByTag(TAG_RESUME_DIALOG) != null) return
        val pages = dataModel.pages.value?.takeIf { it.isNotEmpty() } ?: return

        if (indexOfResumePage() in 1 until pages.size - 1) {
            LessonResumeDialogFragment().show(supportFragmentManager, TAG_RESUME_DIALOG)
        } else {
            resumePageId = null
        }
    }
    // endregion Resume Progress

    // region Feedback
    private fun setupFeedbackDialog() {
        supportFragmentManager.setFragmentResultListener(LessonFeedbackDialogFragment.RESULT_DISMISSED, this) { _, _ ->
            finish()
        }
        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    showFeedbackDialogIfNecessary()
                }
            }.also { cb -> dataModel.showFeedback.observe(this) { cb.isEnabled = it } }
        )
    }

    private fun showFeedbackDialogIfNecessary(): Boolean {
        if (dataModel.showFeedback.value == true) {
            LessonFeedbackDialogFragment(tool, locale, dataModel.pageReached.value).show(supportFragmentManager, null)
            return true
        }
        return false
    }
    // endregion Feedback

    // region Compose Overlays
    private fun LessonActivityBinding.setupComposeOverlays() {
        overlay.setContent {
            GodToolsTheme(darkTheme = false) {
                ContentWithOverlays {
                    val showSwipeTutorial by viewModel.showPageSwipeTutorial.collectAsState(false)
                    if (showSwipeTutorial) {
                        OverlayEffect {
                            delay(800)
                            show(LessonSwipeTutorialAnimatedModalOverlay())
                            settings.setFeatureDiscovered(FEATURE_LESSON_PAGE_SWIPED)
                        }
                    }
                }
            }
        }
    }
    // endregion Compose Overlays
    // endregion UI

    // region Feature Discovery
    private fun LessonActivityBinding.trackPageSwipedFeatureDiscovery() {
        if (settings.isFeatureDiscovered(FEATURE_LESSON_PAGE_SWIPED)) return

        // record that the page was scrolled for feature discovery
        pages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            private var dragging = false
            private var page = 0

            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    SCROLL_STATE_DRAGGING -> {
                        if (!dragging) page = pages.currentItem
                        dragging = true
                    }

                    SCROLL_STATE_IDLE -> {
                        if (dragging && pages.currentItem != page) {
                            settings.setFeatureDiscovered(FEATURE_LESSON_PAGE_SWIPED)

                            // unregister the callback now that we recorded the feature was discovered
                            pages.unregisterOnPageChangeCallback(this)
                        }
                        dragging = false
                    }
                }
            }
        })
    }
    // endregion Feature Discovery

    override fun checkForManifestEvent(manifest: Manifest, event: Event) {
        if (event.id in manifest.dismissListeners && showFeedbackDialogIfNecessary()) return
        super.checkForManifestEvent(manifest, event)
    }

    private fun trackPageInAnalytics(page: LessonPage? = dataModel.pages.value?.getOrNull(binding.pages.currentItem)) {
        page?.let { eventBus.post(LessonPageAnalyticsScreenEvent(page)) }
    }
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class LessonActivityDataModel @Inject constructor(
    downloadManager: GodToolsDownloadManager,
    manifestManager: ManifestManager,
    private val remoteConfig: FirebaseRemoteConfig,
    settings: Settings,
    translationsRepository: TranslationsRepository,
    userActivityManager: UserActivityManager,
    savedState: SavedStateHandle
) : BaseSingleToolActivityDataModel(
    downloadManager,
    manifestManager,
    translationsRepository,
    userActivityManager,
    savedState
) {
    val visiblePages = SetLiveData<String>(synchronous = true)

    val pages = manifest.asLiveData().combineWith(visiblePages) { manifest, visible ->
        manifest?.pages.orEmpty().filterIsInstance<LessonPage>().filter { !it.isHidden || it.id in visible }
    }.distinctUntilChanged()

    val pageReached = savedState.getMutableStateFlow(viewModelScope, "pageReached", 0)
    val showFeedback = toolCode
        .flatMapLatest { settings.isFeatureDiscoveredFlow(FEATURE_LESSON_FEEDBACK + it) }
        .combine(pageReached) { discovered, page -> !discovered && page > 3 }
        .asLiveData()

    internal val showPageSwipeTutorial = settings.isFeatureDiscoveredFlow(FEATURE_LESSON_PAGE_SWIPED)
        .map { !it && remoteConfig.getBoolean(CONFIG_TUTORIAL_LESSON_PAGE_SWIPE) }
}
