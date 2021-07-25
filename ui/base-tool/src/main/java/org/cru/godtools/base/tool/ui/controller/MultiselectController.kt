package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.cru.godtools.base.tool.databinding.ToolContentMultiselectBinding
import org.cru.godtools.base.tool.databinding.ToolContentMultiselectOptionBinding
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.model.Multiselect

class MultiselectController private constructor(
    private val binding: ToolContentMultiselectBinding,
    parentController: BaseController<*>,
    private val optionFactory: OptionController.Factory
) : BaseController<Multiselect>(Multiselect::class, binding.root, parentController) {
    @AssistedInject
    constructor(
        @Assisted parent: ViewGroup,
        @Assisted parentController: BaseController<*>,
        optionFactory: OptionController.Factory
    ) : this(
        ToolContentMultiselectBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        parentController,
        optionFactory
    )

    @AssistedFactory
    interface Factory : BaseController.Factory<MultiselectController>

    // region Lifecycle
    override fun onBind() {
        super.onBind()
        bindOptions()
    }
    // endregion Lifecycle

    // region Options
    private var optionControllers = emptyList<OptionController>()

    private fun bindOptions() {
        optionControllers = binding.options.bindModels(
            model?.options.orEmpty(),
            optionControllers.toMutableList()
        ) { optionFactory.create(binding.options, this) }
    }
    // endregion Options

    class OptionController private constructor(
        private val binding: ToolContentMultiselectOptionBinding,
        parentController: MultiselectController,
        cacheFactory: UiControllerCache.Factory
    ) : ParentController<Multiselect.Option>(Multiselect.Option::class, binding.root, parentController, cacheFactory) {
        @AssistedInject
        internal constructor(
            @Assisted parent: ViewGroup,
            @Assisted parentController: MultiselectController,
            cacheFactory: UiControllerCache.Factory
        ) : this(
            binding = ToolContentMultiselectOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            parentController = parentController,
            cacheFactory = cacheFactory
        )

        @AssistedFactory
        interface Factory {
            fun create(parent: ViewGroup, multiselectController: MultiselectController): OptionController
        }

        init {
            binding.lifecycleOwner = lifecycleOwner
            binding.controller = this
        }

        override val childContainer get() = binding.content

        override fun onBind() {
            super.onBind()
            binding.model = model
            binding.isSelected = model?.isSelectedFlow(toolState)?.asLiveData() ?: ImmutableLiveData(false)
        }

        override val textEnableTextIsSelectable get() = false

        fun toggleOption() = model?.toggleSelected(toolState)
    }
}
