package org.cru.godtools.tool.lesson.ui

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.overlay.OverlayEffect
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okio.FileSystem
import org.ccci.gto.android.common.androidx.lifecycle.SetLiveData
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.getMutableStateFlow
import org.cru.godtools.base.CONFIG_TUTORIAL_LESSON_PAGE_SWIPE
import org.cru.godtools.base.HOST_DYNALINKS
import org.cru.godtools.base.HOST_GODTOOLSAPP_COM
import org.cru.godtools.base.SCHEME_GODTOOLS
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_LESSON_FEEDBACK
import org.cru.godtools.base.Settings.Companion.FEATURE_LESSON_PAGE_SWIPED
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.TOOL_RESOURCE_FILE_SYSTEM
import org.cru.godtools.base.tool.EXTRA_RESUME_PAGE
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity
import org.cru.godtools.base.tool.activity.BaseSingleToolActivityDataModel
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.downloadmanager.GodToolsDownloadManager
import org.cru.godtools.shared.renderer.lesson.LessonScreen
import org.cru.godtools.shared.renderer.lesson.RenderLesson
import org.cru.godtools.shared.renderer.lesson.rememberLessonPagerState
import org.cru.godtools.shared.renderer.tips.TipsRepository
import org.cru.godtools.shared.renderer.util.ProvideRendererServices
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.lesson.LessonPage
import org.cru.godtools.tool.lesson.BuildConfig.HOST_GODTOOLS_CUSTOM_URI
import org.cru.godtools.tool.lesson.R
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
    ) {

    @Inject
    internal lateinit var toolsRepository: ToolsRepository

    @Inject
    @Named(TOOL_RESOURCE_FILE_SYSTEM)
    lateinit var resourceFileSystem: FileSystem
    @Inject
    lateinit var tipsRepository: TipsRepository

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
        binding.compose.setContent {
            val pagerState = rememberLessonPagerState()
            val eventSink: (LessonScreen.UiEvent) -> Unit = {
                when (it) {
                    LessonScreen.UiEvent.CloseLesson -> {
                        if (!showFeedbackDialogIfNecessary()) {
                            finish()
                        }
                    }
                }
            }

            // record the highest page reached for feedback functionality
            LaunchedEffect(Unit) {
                snapshotFlow { pagerState.settledPage }.collect { page ->
                    dataModel.pageReached.value = maxOf(page, dataModel.pageReached.value)
                }
            }

            // record the current progress for lesson resume functionality
            LaunchedEffect(Unit) {
                snapshotFlow { pagerState.settledPage }.collect {
                    // TODO: this isn't properly capturing the page id
                    updateProgress(it)
                }
            }

            // determine the UI Rendering state
            val loadingState = activeToolLoadingStateLiveData.observeAsState().value
            val manifest = dataModel.manifest.collectAsState().value
            val state = when {
                loadingState == LoadingState.OFFLINE -> LessonScreen.UiState.Offline(eventSink)
                loadingState == LoadingState.NOT_FOUND || loadingState == LoadingState.INVALID_TYPE ->
                    LessonScreen.UiState.Missing(eventSink)
                manifest == null || loadingState == LoadingState.LOADING -> {
                    val downloadProgress by viewModel.downloadProgress.collectAsState()
                    val progress by remember {
                        derivedStateOf {
                            downloadProgress?.takeUnless { it.isIndeterminate }?.let { it.progress.toFloat() / it.max }
                        }
                    }
                    LessonScreen.UiState.Loading(progress, eventSink)
                }
                else -> LessonScreen.UiState.Loaded(
                    manifest = manifest,
                    state = toolState.toolState,
                    pagerState = pagerState,
                    eventSink = eventSink
                )
            }

            // render the Lesson
            ProvideRendererServices(resources = resourceFileSystem, tipsRepository = tipsRepository) {
                GodToolsTheme(darkTheme = false) {
                    ContentWithOverlays {
                        RenderLesson(state)

                        // swipe tutorial Overlay
                        // TODO: figure out a more scalable way to handle multiple different overlays
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
    // region Progress
    private fun updateProgress(
        position: Int,
        // TODO: this isn't an accurate list of pages
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
    }
    // endregion Progress

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
            // TODO: figure out how to navigate the pager to the correct page
            indexOfResumePage().takeIf { it >= 0 } // ?.let { binding.pages.currentItem = it }
            resumePageId = null
        }
        supportFragmentManager.setFragmentResultListener(LessonResumeDialogFragment.RESULT_RESTART, this) { _, _ ->
            resumePageId = null
        }

        // TODO: figure out a new trigger condition for the resume dialog
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
    // endregion UI

    override fun checkForManifestEvent(manifest: Manifest, event: Event) {
        if (event.id in manifest.dismissListeners && showFeedbackDialogIfNecessary()) return
        super.checkForManifestEvent(manifest, event)
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
