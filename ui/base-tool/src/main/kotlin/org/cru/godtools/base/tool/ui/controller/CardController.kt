package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.databinding.ToolContentCardBinding
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.model.Card

class CardController private constructor(
    private val binding: ToolContentCardBinding,
    parentController: BaseController<*>,
    cacheFactory: UiControllerCache.Factory
) : ParentController<Card>(Card::class, binding.root, parentController, cacheFactory) {
    @AssistedInject
    internal constructor(
        @Assisted parent: ViewGroup,
        @Assisted parentController: BaseController<*>,
        cacheFactory: UiControllerCache.Factory
    ) : this(
        ToolContentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        parentController,
        cacheFactory
    )

    @AssistedFactory
    interface Factory : BaseController.Factory<CardController>

    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.controller = this
    }

    override fun onBind() {
        super.onBind()
        binding.model = model
    }

    override val childContainer get() = binding.content
}
