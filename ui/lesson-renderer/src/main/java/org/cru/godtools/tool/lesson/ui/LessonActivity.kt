package org.cru.godtools.tool.lesson.ui

import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity
import org.cru.godtools.tool.lesson.R
import org.cru.godtools.tool.lesson.databinding.LessonActivityBinding
import org.cru.godtools.xml.model.Manifest

@AndroidEntryPoint
class LessonActivity : BaseSingleToolActivity<LessonActivityBinding>(
    contentLayoutId = R.layout.lesson_activity,
    requireTool = true,
    supportedType = Manifest.Type.LESSON
)
