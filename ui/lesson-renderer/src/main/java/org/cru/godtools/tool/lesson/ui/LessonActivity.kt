package org.cru.godtools.tool.lesson.ui

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.switchMap
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.lifecycle.SetLiveData
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.viewpager2.widget.whileMaintainingVisibleCurrentItem
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_LESSON_FEEDBACK
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity
import org.cru.godtools.base.tool.activity.BaseSingleToolActivityDataModel
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.tool.viewmodel.ToolStateHolder
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.tool.lesson.R
import org.cru.godtools.tool.lesson.analytics.model.LessonPageAnalyticsScreenEvent
import org.cru.godtools.tool.lesson.databinding.LessonActivityBinding
import org.cru.godtools.tool.lesson.ui.feedback.LessonFeedbackDialogFragment
import org.cru.godtools.tool.lesson.util.isLessonDeepLink
import org.cru.godtools.tool.lesson.util.lessonDeepLinkCode
import org.cru.godtools.tool.lesson.util.lessonDeepLinkLocale
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.lesson.LessonPage
import org.keynote.godtools.android.db.GodToolsDao

@AndroidEntryPoint
class LessonActivity :
    BaseSingleToolActivity<LessonActivityBinding>(
        contentLayoutId = R.layout.lesson_activity,
        requireTool = true,
        supportedType = Manifest.Type.LESSON
    ),
    LessonPageAdapter.Callbacks {
    override val dataModel: LessonActivityDataModel by viewModels()
    private val toolState: ToolStateHolder by viewModels()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
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
    override fun processIntent(intent: Intent?, savedInstanceState: Bundle?) {
        super.processIntent(intent, savedInstanceState)
        val data = intent?.data
        when (intent?.action) {
            ACTION_VIEW -> when {
                data?.isLessonDeepLink() == true -> {
                    dataModel.toolCode.value = data.lessonDeepLinkCode
                    dataModel.locale.value = data.lessonDeepLinkLocale
                }
            }
        }
    }
    // endregion Intent Processing

    // region UI
    // region Toolbar
    override val toolbar get() = binding.appbar

    override fun updateToolbarTitle() {
        title = ""
    }
    // endregion Toolbar

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
                dataModel.pageReached.value = maxOf(position, dataModel.pageReached.value ?: 0)
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
        LessonFeedbackDialogFragment(tool, locale, dataModel.pageReached.value ?: 0).show(supportFragmentManager, null)
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
class LessonActivityDataModel @Inject constructor(
    manifestManager: ManifestManager,
    dao: GodToolsDao,
    downloadManager: GodToolsDownloadManager,
    settings: Settings,
    savedState: SavedStateHandle
) : BaseSingleToolActivityDataModel(manifestManager, dao, downloadManager) {
    val visiblePages = SetLiveData<String>(synchronous = true)

    val pages = manifest.combineWith(visiblePages) { manifest, visible ->
        manifest?.pages.orEmpty().filterIsInstance<LessonPage>().filter { !it.isHidden || it.id in visible }
    }.distinctUntilChanged()

    val pageReached = savedState.getLiveData("pageReached", 0)
    val showFeedback = toolCode
        .switchMap { settings.isFeatureDiscoveredLiveData(FEATURE_LESSON_FEEDBACK + it) }
        .combineWith(pageReached) { discovered, page -> !discovered && page > 3 }
        .distinctUntilChanged()
}
