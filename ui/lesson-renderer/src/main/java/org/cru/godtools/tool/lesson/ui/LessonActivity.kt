package org.cru.godtools.tool.lesson.ui

import android.os.Bundle
import androidx.annotation.MainThread
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.ccci.gto.android.common.eventbus.lifecycle.register
import org.cru.godtools.base.model.Event
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity
import org.cru.godtools.tool.lesson.R
import org.cru.godtools.tool.lesson.databinding.LessonActivityBinding
import org.cru.godtools.xml.model.Manifest
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class LessonActivity : BaseSingleToolActivity<LessonActivityBinding>(
    contentLayoutId = R.layout.lesson_activity,
    requireTool = true,
    supportedType = Manifest.Type.LESSON
) {
    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventBus.register(this, this)
    }

    override fun onContentChanged() {
        super.onContentChanged()
        binding.setupPages()
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onContentEvent(event: Event) {
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

    // region Pages
    @Inject
    lateinit var lessonPageAdapterFactory: LessonPageAdapter.Factory
    private val lessonPageAdapter by lazy {
        lessonPageAdapterFactory.create(this).also { activeManifestLiveData.observe(this, it) }
    }

    private fun LessonActivityBinding.setupPages() {
        pages.adapter = lessonPageAdapter
    }

    private fun checkForPageEvent(event: Event) {
        val page = activeManifestLiveData.value?.lessonPages?.firstOrNull { it.listeners.contains(event.id) }
        if (page != null) {
            binding.pages.currentItem = page.position
        }
    }
    // endregion Pages
    // endregion UI
}
