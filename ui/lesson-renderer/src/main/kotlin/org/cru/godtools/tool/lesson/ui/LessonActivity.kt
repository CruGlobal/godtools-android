package org.cru.godtools.tool.lesson.ui

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
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
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import okio.FileSystem
import org.ccci.gto.android.common.androidx.lifecycle.getMutableStateFlow
import org.cru.godtools.base.CONFIG_TUTORIAL_LESSON_PAGE_SWIPE
import org.cru.godtools.base.HOST_DYNALINKS
import org.cru.godtools.base.HOST_GODTOOLSAPP_COM
import org.cru.godtools.base.SCHEME_GODTOOLS
import org.cru.godtools.base.Settings
import org.cru.godtools.base.URI_SHARE_BASE
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
import org.cru.godtools.tool.lesson.analytics.model.LessonFeedbackAnalyticsEvent
import org.cru.godtools.tool.lesson.databinding.LessonActivityBinding
import org.cru.godtools.tool.lesson.ui.feedback.LessonFeedbackDialogOverlay
import org.cru.godtools.tool.lesson.ui.resume.LessonResumeDialogOverlay
import org.cru.godtools.tool.lesson.ui.swipetutorial.LessonSwipeTutorialAnimatedModalOverlay
import org.cru.godtools.tool.lesson.util.isLessonDeepLink
import org.cru.godtools.user.activity.UserActivityManager

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
    }

    override fun inflateBinding() = LessonActivityBinding.inflate(layoutInflater, null, false)

    override fun onBindingChanged() {
        super.onBindingChanged()
        binding.compose.setContent {
            var resumePageId by rememberSaveable { mutableStateOf(intent?.getStringExtra(EXTRA_RESUME_PAGE)) }

            val showFeedback by dataModel.showFeedback.observeAsState(false)
            var showFeedbackDialog by rememberSaveable { mutableStateOf(false) }

            // handle dismiss events
            val manifest = dataModel.manifest.collectAsState().value
            LaunchedEffect(toolState.toolState, manifest) {
                toolState.toolState.contentEvents
                    .filter { it in manifest?.dismissListeners.orEmpty() }
                    .collect {
                        if (showFeedback) {
                            showFeedbackDialog = true
                        } else {
                            finish()
                        }
                    }
            }

            // determine the UI Rendering state
            val loadingState = activeToolLoadingStateLiveData.observeAsState().value
            val eventSink: (LessonScreen.UiEvent) -> Unit = {
                when (it) {
                    LessonScreen.UiEvent.CloseLesson -> {
                        if (showFeedback) {
                            showFeedbackDialog = true
                        } else {
                            finish()
                        }
                    }
                }
            }
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

                else -> {
                    val lessonPagerState = rememberLessonPagerState(manifest)
                    val pagerState = lessonPagerState.pagerState

                    // record the highest page reached for feedback functionality
                    LaunchedEffect(pagerState) {
                        snapshotFlow { pagerState.settledPage }.collect { page ->
                            dataModel.pageReached.update { maxOf(it, page) }
                        }
                    }

                    // record the current progress for lesson resume functionality
                    if (resumePageId == null) {
                        LaunchedEffect(pagerState) {
                            snapshotFlow { pagerState.settledPage }
                                .conflate()
                                .collect { updateProgress(it, lessonPagerState.pages) }
                        }
                    }

                    // record feature discovery on page swipe
                    LaunchedEffect(pagerState) {
                        val initialPage = pagerState.settledPage
                        snapshotFlow { pagerState.settledPage }
                            .filter { it != initialPage }
                            .first()
                        settings.setFeatureDiscovered(FEATURE_LESSON_PAGE_SWIPED)
                    }

                    LessonScreen.UiState.Loaded(
                        manifest = manifest,
                        state = toolState.toolState,
                        lessonPager = lessonPagerState,
                        eventSink = eventSink
                    )
                }
            }

            val shareLinkUri by shareLinkUriLiveData.observeAsState()

            // render the Lesson
            ProvideRendererServices(resources = resourceFileSystem, tipsRepository = tipsRepository) {
                GodToolsTheme(darkTheme = false) {
                    ContentWithOverlays {
                        RenderLesson(
                            state = state,
                            onShareClick = if (shareLinkUri != null && state is LessonScreen.UiState.Loaded) {
                                { shareCurrentTool() }
                            } else null
                        )

                        // resume lesson progress dialog
                        if (state is LessonScreen.UiState.Loaded && resumePageId != null) {
                            OverlayEffect(resumePageId) {
                                val pageId = resumePageId ?: return@OverlayEffect
                                if (state.indexOfResumePage(pageId) in 1 until state.lessonPager.pages.size - 1) {
                                    val result = show(LessonResumeDialogOverlay(pageId))
                                    if (result is LessonResumeDialogOverlay.Result.Resume) {
                                        val index = state.indexOfResumePage(result.pageId)
                                        if (index >= 0) state.lessonPager.pagerState.animateScrollToPage(index)
                                    }
                                }

                                resumePageId = null
                            }
                        }

                        // swipe tutorial Overlay
                        // TODO: figure out a more scalable way to handle multiple different overlays
                        val showSwipeTutorial by viewModel.showPageSwipeTutorial.collectAsState(false)
                        if (state is LessonScreen.UiState.Loaded && showSwipeTutorial) {
                            OverlayEffect {
                                delay(800)
                                show(LessonSwipeTutorialAnimatedModalOverlay())
                                settings.setFeatureDiscovered(FEATURE_LESSON_PAGE_SWIPED)
                            }
                        }

                        // feedback overlay
                        BackHandler(enabled = showFeedback) { showFeedbackDialog = true }
                        if (showFeedbackDialog) {
                            val pageReached by dataModel.pageReached.collectAsState()
                            OverlayEffect {
                                val result = show(LessonFeedbackDialogOverlay(pageReached))
                                if (result is LessonFeedbackDialogOverlay.Result.Submit) {
                                    eventBus.post(
                                        LessonFeedbackAnalyticsEvent(
                                            tool,
                                            locale,
                                            pageReached = result.pageReached,
                                            helpful = result.helpful,
                                            readiness = result.readiness
                                        )
                                    )
                                }
                                settings.setFeatureDiscovered(FEATURE_LESSON_FEEDBACK + tool)
                                finish()
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
    override val statusBarStyle = SystemBarStyle.light(
        // EdgeToEdge.DefaultLightScrim
        scrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF),
        // EdgeToEdge.DefaultDarkScrim
        darkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    )

    // region Progress
    private suspend fun updateProgress(position: Int, pages: List<LessonPage>) {
        toolsRepository.updateToolProgress(
            tool,
            if (pages.isEmpty()) 0.0 else (position.toDouble() / pages.size),
            pages.getOrNull(position)?.id
        )
    }
    // endregion Progress

    // region Resume Progress
    private fun LessonScreen.UiState.Loaded.indexOfResumePage(pageId: String?) = manifest.findPage(pageId)
        ?.let { generateSequence(it) { it.previousPage }.firstOrNull { !it.isHidden } }
        ?.let { return lessonPager.pages.indexOf(it) } ?: -1
    // endregion Resume Progress

    // region Share Menu Logic
    override val shareLinkUriLiveData by lazy {
        viewModel.manifest.map { it?.buildShareLink()?.build()?.toString() }.asLiveData()
    }

    private fun Manifest.buildShareLink(): Uri.Builder? {
        val tool = code ?: return null
        val locale = locale ?: return null
        return URI_SHARE_BASE.buildUpon()
            .appendEncodedPath(locale.toString().lowercase(Locale.ENGLISH))
            .appendPath("tool")
            .appendPath("lesson")
            .appendPath(tool)
            .appendQueryParameter("icid", "gtshare")
    }
    // endregion Share Menu Logic
    // endregion UI

    override fun checkForManifestEvent(manifest: Manifest, event: Event) {
        // suppress dismissListeners which are handled elsewhere
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
    val pageReached = savedState.getMutableStateFlow(viewModelScope, "pageReached", 0)
    val showFeedback = toolCode
        .flatMapLatest { settings.isFeatureDiscoveredFlow(FEATURE_LESSON_FEEDBACK + it) }
        .combine(pageReached) { discovered, page -> !discovered && page > 3 }
        .asLiveData()

    internal val showPageSwipeTutorial = settings.isFeatureDiscoveredFlow(FEATURE_LESSON_PAGE_SWIPED)
        .map { !it && remoteConfig.getBoolean(CONFIG_TUTORIAL_LESSON_PAGE_SWIPE) }
}
