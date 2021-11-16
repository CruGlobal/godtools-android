package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.databinding.ToolContentFlowBinding
import org.cru.godtools.base.tool.databinding.ToolContentFlowItemBinding
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.model.Flow

class FlowController private constructor(
    private val binding: ToolContentFlowBinding,
    parentController: BaseController<*>,
    private val itemFactory: ItemController.Factory
) : BaseController<Flow>(Flow::class, binding.root, parentController) {
    @AssistedInject
    constructor(
        @Assisted parent: ViewGroup,
        @Assisted parentController: BaseController<*>,
        optionFactory: ItemController.Factory
    ) : this(
        ToolContentFlowBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        parentController,
        optionFactory
    )

    @AssistedFactory
    interface Factory : BaseController.Factory<FlowController>

    // region Lifecycle
    override fun onBind() {
        super.onBind()
        bindItems()
    }
    // endregion Lifecycle

    // region Items
    private var itemControllers = emptyList<ItemController>()

    private fun bindItems() {
        itemControllers = binding.container.bindModels(
            model?.items.orEmpty(),
            itemControllers.toMutableList()
        ) { itemFactory.create(binding.container, this) }

        binding.flow.referencedIds = itemControllers.map { it.root.id }.toIntArray()
    }
    // endregion Items

    class ItemController private constructor(
        private val binding: ToolContentFlowItemBinding,
        parentController: FlowController,
        cacheFactory: UiControllerCache.Factory
    ) : ParentController<Flow.Item>(Flow.Item::class, binding.root, parentController, cacheFactory) {
        @AssistedInject
        internal constructor(
            @Assisted parent: ViewGroup,
            @Assisted parentController: FlowController,
            cacheFactory: UiControllerCache.Factory
        ) : this(
            binding = ToolContentFlowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            parentController = parentController,
            cacheFactory = cacheFactory
        )

        @AssistedFactory
        interface Factory {
            fun create(parent: ViewGroup, flowController: FlowController): ItemController
        }

        init {
            binding.root.id = View.generateViewId()
        }

        override val childContainer get() = binding.content

        override fun onBind() {
            super.onBind()
            binding.model = model
        }
    }
}
