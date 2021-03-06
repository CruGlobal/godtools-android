package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.databinding.ToolContentParagraphBinding
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.model.Paragraph

class ParagraphController private constructor(
    private val binding: ToolContentParagraphBinding,
    parentController: BaseController<*>,
    cacheFactory: UiControllerCache.Factory
) : ParentController<Paragraph>(Paragraph::class, binding.content, parentController, cacheFactory) {
    @AssistedInject
    internal constructor(
        @Assisted parent: ViewGroup,
        @Assisted parentController: BaseController<*>,
        cacheFactory: UiControllerCache.Factory
    ) : this(
        ToolContentParagraphBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        parentController,
        cacheFactory
    )

    @AssistedFactory
    interface Factory : BaseController.Factory<ParagraphController>

    override val contentContainer get() = binding.content
}
