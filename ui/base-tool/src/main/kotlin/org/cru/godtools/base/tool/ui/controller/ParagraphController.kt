package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.databinding.ToolContentParagraphBinding
import org.cru.godtools.tool.model.Paragraph

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
        binding.isGone = model?.isGoneFlow(toolState)?.asLiveData()
        binding.isInvisible = model?.isInvisibleFlow(toolState)?.asLiveData()
    }

    override val childContainer get() = binding.content
}
