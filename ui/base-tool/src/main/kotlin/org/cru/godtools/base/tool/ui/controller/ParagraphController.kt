package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.model.isGoneStateFlow
import org.cru.godtools.base.tool.model.isInvisibleStateFlow
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.shared.tool.parser.model.Paragraph
import org.cru.godtools.tool.databinding.ToolContentParagraphBinding

class ParagraphController private constructor(
    private val binding: ToolContentParagraphBinding,
    parentController: BaseController<*>,
    cacheFactory: UiControllerCache.Factory
) : ParentController<Paragraph>(Paragraph::class, binding.root, parentController, cacheFactory) {
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

    init {
        binding.lifecycleOwner = lifecycleOwner
    }

    override fun onBind() {
        super.onBind()
        binding.isGone = model?.isGoneStateFlow(toolState, lifecycleOwner.lifecycleScope)
        binding.isInvisible = model?.isInvisibleStateFlow(toolState, lifecycleOwner.lifecycleScope)
    }

    override val childContainer get() = binding.content
}
