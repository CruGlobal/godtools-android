package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.BR
import org.cru.godtools.tool.databinding.ToolContentMultiselectBinding
import org.cru.godtools.tool.databinding.ToolContentMultiselectOptionCardBinding
import org.cru.godtools.tool.databinding.ToolContentMultiselectOptionFlatBinding
import org.cru.godtools.tool.model.AnalyticsEvent
import org.cru.godtools.tool.model.Base
import org.cru.godtools.tool.model.Multiselect

class MultiselectController private constructor(
    private val binding: ToolContentMultiselectBinding,
    parentController: BaseController<*>,
    private val cacheFactory: UiControllerCache.Factory
) : BaseController<Multiselect>(Multiselect::class, binding.root, parentController) {
    @AssistedInject
    constructor(
        @Assisted parent: ViewGroup,
        @Assisted parentController: BaseController<*>,
        cacheFactory: UiControllerCache.Factory
    ) : this(
        ToolContentMultiselectBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        parentController,
        cacheFactory
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
    private val optionCache by lazy { cacheFactory.create(binding.options, this) }
    private var optionControllers = emptyList<BaseController<Multiselect.Option>>()

    private fun bindOptions() {
        optionControllers = binding.options.bindModels(
            models = model?.options.orEmpty(),
            existing = optionControllers.toMutableList(),
            releaseController = { it.releaseTo(optionCache) }
        ) { optionCache.acquire(it) }

        binding.flow.referencedIds = optionControllers.map { it.root.id }.toIntArray()
    }
    // endregion Options

    sealed class OptionController<B : ViewDataBinding>(
        protected val binding: B,
        parentController: BaseController<*>,
        cacheFactory: UiControllerCache.Factory
    ) : ParentController<Multiselect.Option>(Multiselect.Option::class, binding.root, parentController, cacheFactory) {
        init {
            binding.root.id = View.generateViewId()
            binding.lifecycleOwner = lifecycleOwner
            binding.setVariable(BR.controller, this)
        }

        override fun onBind() {
            super.onBind()
            binding.setVariable(BR.model, model)
            binding.setVariable(
                BR.isSelected,
                model?.isSelectedFlow(toolState)?.asLiveData() ?: ImmutableLiveData(false)
            )
        }

        override val isClickable = true

        fun toggleOption() {
            triggerAnalyticsEvents(model?.getAnalyticsEvents(AnalyticsEvent.Trigger.CLICKED))
            model?.toggleSelected(toolState)
        }

        class CardOptionController(
            binding: ToolContentMultiselectOptionCardBinding,
            parentController: BaseController<*>,
            cacheFactory: UiControllerCache.Factory
        ) : OptionController<ToolContentMultiselectOptionCardBinding>(binding, parentController, cacheFactory) {
            @AssistedInject
            internal constructor(
                @Assisted parent: ViewGroup,
                @Assisted parentController: BaseController<*>,
                cacheFactory: UiControllerCache.Factory
            ) : this(
                ToolContentMultiselectOptionCardBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                parentController = parentController,
                cacheFactory = cacheFactory
            )

            @AssistedFactory
            interface Factory : BaseController.Factory<CardOptionController>

            override val childContainer get() = binding.content

            override fun supportsModel(model: Base) =
                model is Multiselect.Option && model.style == Multiselect.Option.Style.CARD
        }

        class FlatOptionController(
            binding: ToolContentMultiselectOptionFlatBinding,
            parentController: BaseController<*>,
            cacheFactory: UiControllerCache.Factory
        ) : OptionController<ToolContentMultiselectOptionFlatBinding>(binding, parentController, cacheFactory) {
            @AssistedInject
            internal constructor(
                @Assisted parent: ViewGroup,
                @Assisted parentController: BaseController<*>,
                cacheFactory: UiControllerCache.Factory
            ) : this(
                ToolContentMultiselectOptionFlatBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                parentController = parentController,
                cacheFactory = cacheFactory
            )

            @AssistedFactory
            interface Factory : BaseController.Factory<FlatOptionController>

            override val childContainer get() = binding.content

            override fun supportsModel(model: Base) =
                model is Multiselect.Option && model.style == Multiselect.Option.Style.FLAT
        }
    }
}
