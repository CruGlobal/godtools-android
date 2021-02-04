package org.cru.godtools.tool.lesson.ui

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity
import org.cru.godtools.tool.lesson.R
import org.cru.godtools.tool.lesson.databinding.LessonActivityBinding
import org.cru.godtools.xml.model.Manifest

@AndroidEntryPoint
class LessonActivity : BaseSingleToolActivity<LessonActivityBinding>(
    contentLayoutId = R.layout.lesson_activity,
    requireTool = true,
    supportedType = Manifest.Type.LESSON
) {
    // region Lifecycle
    override fun onContentChanged() {
        super.onContentChanged()
        binding.setupPages()
    }
    // endregion Lifecycle

    // region Pages
    @Inject
    lateinit var lessonPageAdapterFactory: LessonPageAdapter.Factory
    private val lessonPageAdapter by lazy {
        lessonPageAdapterFactory.create(this).also { activeManifestLiveData.observe(this, it) }
    }

    private fun LessonActivityBinding.setupPages() {
        pages.adapter = lessonPageAdapter
    }
    // endregion Pages
}
