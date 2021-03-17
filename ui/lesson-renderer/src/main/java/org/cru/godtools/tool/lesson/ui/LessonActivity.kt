package org.cru.godtools.tool.lesson.ui

import androidx.activity.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.SetLiveData
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.viewpager2.widget.whileMaintainingVisibleCurrentItem
import org.cru.godtools.base.model.Event
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity
import org.cru.godtools.base.tool.activity.BaseSingleToolActivityDataModel
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.tool.lesson.R
import org.cru.godtools.tool.lesson.analytics.model.LessonPageAnalyticsScreenEvent
import org.cru.godtools.tool.lesson.databinding.LessonActivityBinding
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.lesson.LessonPage
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

    // region Lifecycle
    override fun onBindingChanged() {
        super.onBindingChanged()
        binding.setupPages()
        setupProgressIndicator()
    }

    override fun onResume() {
        super.onResume()
        trackPageInAnalytics()
    }

    override fun onContentEvent(event: Event) {
        checkForPageEvent(event)
    }
    // endregion Lifecycle

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
    private val lessonPageAdapter by lazy { lessonPageAdapterFactory.create(this).also { it.callbacks = this } }

    private fun LessonActivityBinding.setupPages() {
        pages.adapter = lessonPageAdapter
        dataModel.pages.observe(this@LessonActivity) { lessonPages ->
            pages.whileMaintainingVisibleCurrentItem { lessonPageAdapter.pages = lessonPages }
        }

        pages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == SCROLL_STATE_IDLE) {
                    val currentItemId = dataModel.pages.value?.getOrNull(pages.currentItem)?.id
                    dataModel.visiblePages.removeAll { it != currentItemId }
                    updateProgressIndicator()
                }
            }

            override fun onPageSelected(position: Int) {
                updateProgressIndicator(position = position)
                trackPageInAnalytics(dataModel.pages.value?.getOrNull(position))
            }
        })
    }

    private fun checkForPageEvent(event: Event) {
        val page = dataModel.manifest.value?.lessonPages?.firstOrNull { it.listeners.contains(event.id) }
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
    // endregion UI

    private fun trackPageInAnalytics(page: LessonPage? = dataModel.pages.value?.getOrNull(binding.pages.currentItem)) {
        page?.let { eventBus.post(LessonPageAnalyticsScreenEvent(page)) }
    }
}

@HiltViewModel
class LessonActivityDataModel @Inject constructor(
    manifestManager: ManifestManager,
    dao: GodToolsDao,
    downloadManager: GodToolsDownloadManager
) : BaseSingleToolActivityDataModel(manifestManager, dao, downloadManager) {
    val visiblePages = SetLiveData<String>(synchronous = true)

    val pages = manifest.combineWith(visiblePages) { manifest, visible ->
        manifest?.lessonPages.orEmpty().filter { !it.isHidden || it.id in visible }
    }.distinctUntilChanged()
}
