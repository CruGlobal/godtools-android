package org.cru.godtools.tool.lesson.ui

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.lifecycle.SetLiveData
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.getMutableStateFlow
import org.ccci.gto.android.common.androidx.viewpager2.widget.whileMaintainingVisibleCurrentItem
import org.cru.godtools.base.DAGGER_HOST_CUSTOM_URI
import org.cru.godtools.base.SCHEME_GODTOOLS
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_LESSON_FEEDBACK
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity
import org.cru.godtools.base.tool.activity.BaseSingleToolActivityDataModel
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.tool.viewmodel.ToolStateHolder
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.lesson.LessonPage
import org.cru.godtools.tool.lesson.R
import org.cru.godtools.tool.lesson.analytics.model.LessonPageAnalyticsScreenEvent
import org.cru.godtools.tool.lesson.databinding.LessonActivityBinding
import org.cru.godtools.tool.lesson.ui.feedback.LessonFeedbackDialogFragment
import org.cru.godtools.tool.lesson.util.isLessonDeepLink
import org.cru.godtools.user.activity.UserActivityManager
import org.keynote.godtools.android.db.repository.TranslationsRepository

@AndroidEntryPoint
class LessonActivity :
    BaseSingleToolActivity<LessonActivityBinding>(
        contentLayoutId = R.layout.lesson_activity,
        requireTool = true,
        supportedType = Manifest.Type.LESSON
    ),
    LessonPageAdapter.Callbacks {
    override val viewModel: LessonActivityDataModel by viewModels()
    override val dataModel get() = viewModel
    private val toolState: ToolStateHolder by viewModels()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        if (savedInstanceState == null) trackToolOpen(tool, Manifest.Type.LESSON)
        setupFeedbackDialog()
    }

    override fun onBindingChanged() {
        super.onBindingChanged()
        binding.setupPages()
        setupProgressIndicator()
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
    @Inject
    @Named(DAGGER_HOST_CUSTOM_URI)
    internal lateinit var hostCustomUriScheme: String

    override fun processIntent(intent: Intent, savedInstanceState: Bundle?) {
        super.processIntent(intent, savedInstanceState)
        val data = intent.data?.normalizeScheme() ?: return
        val path = data.pathSegments ?: return

        when (intent.action) {
            ACTION_VIEW -> when {
                // Sample Lesson deep link: https://godtoolsapp.com/lessons/lessonholyspirit/en
                data.isLessonDeepLink() -> {
                    dataModel.toolCode.value = path[1]
                    dataModel.locale.value = Locale.forLanguageTag(path[2])
                }
                // Sample deep link: godtools://org.cru.godtools/tool/lesson/{tool}/{locale}
                data.isCustomUriDeepLink() -> {
                    dataModel.toolCode.value = path[2]
                    dataModel.locale.value = Locale.forLanguageTag(path[3])
                }
            }
        }
    }

    private fun Uri.isCustomUriDeepLink() = scheme == SCHEME_GODTOOLS &&
        hostCustomUriScheme.equals(host, true) && pathSegments.orEmpty().size >= 4 &&
        pathSegments?.getOrNull(0) == "tool" && pathSegments?.getOrNull(1) == "lesson"
    // endregion Intent Processing

    // region UI
    override val toolbar get() = binding.appbar

    // region Progress Indicator
    private fun setupProgressIndicator() {
        dataModel.pages.observe(this@LessonActivity) { updateProgressIndicator(pages = it) }
    }

    private fun updateProgressIndicator(
        position: Int = binding.pages.currentItem,
        pages: List<LessonPage>? = dataModel.pages.value
    ) {
        binding.progress.max = pages?.count { !it.isHidden } ?: 0
        // TODO: switch to setProgressCompat(p, true) once this bug is fixed:
        //       https://github.com/material-components/material-components-android/issues/2051
        binding.progress.progress = pages?.take(position + 1)?.count { !it.isHidden } ?: 0
    }
    // endregion Progress Indicator

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
                updateProgressIndicator(position = position)
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

    private fun showFeedbackDialogIfNecessary() = if (dataModel.showFeedback.value == true) {
        LessonFeedbackDialogFragment(tool, locale, dataModel.pageReached.value).show(supportFragmentManager, null)
        true
    } else false
    // endregion Feedback
    // endregion UI

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
}
