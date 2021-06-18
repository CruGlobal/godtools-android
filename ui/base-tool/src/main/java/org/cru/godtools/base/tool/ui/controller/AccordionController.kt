package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.databinding.ToolContentAccordionBinding
import org.cru.godtools.base.tool.databinding.ToolContentAccordionSectionBinding
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.model.Accordion

class AccordionController private constructor(
    private val binding: ToolContentAccordionBinding,
    parentController: BaseController<*>,
    private val sectionFactory: SectionController.Factory
) : BaseController<Accordion>(Accordion::class, binding.root, parentController) {
    @AssistedInject
    constructor(
        @Assisted parent: ViewGroup,
        @Assisted parentController: BaseController<*>,
        sectionFactory: SectionController.Factory
    ) : this(
        ToolContentAccordionBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        parentController,
        sectionFactory
    )

    @AssistedFactory
    interface Factory : BaseController.Factory<AccordionController>

    // region Lifecycle
    override fun onBind() {
        super.onBind()
        bindSections()
    }
    // endregion Lifecycle

    // region Sections
    internal var activeSection = MutableLiveData<String?>(null)
    private var sectionControllers = emptyList<SectionController>()

    private fun bindSections() {
        // update the active section
        if (activeSection.value !in model?.sections?.map { it.id }.orEmpty()) activeSection.value = null

        sectionControllers = binding.sections.bindModels(
            model?.sections.orEmpty(),
            sectionControllers.toMutableList(),
            acquireController = { sectionFactory.create(binding.sections, this) }
        )
    }
    // endregion Sections

    class SectionController private constructor(
        private val binding: ToolContentAccordionSectionBinding,
        private val accordionController: AccordionController,
        cacheFactory: UiControllerCache.Factory
    ) : ParentController<Accordion.Section>(Accordion.Section::class, binding.root, accordionController, cacheFactory) {
        @AssistedInject
        internal constructor(
            @Assisted parent: ViewGroup,
            @Assisted accordionController: AccordionController,
            cacheFactory: UiControllerCache.Factory
        ) : this(
            binding = ToolContentAccordionSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            accordionController = accordionController,
            cacheFactory = cacheFactory
        )

        @AssistedFactory
        interface Factory {
            fun create(parent: ViewGroup, accordionController: AccordionController): SectionController
        }

        init {
            binding.lifecycleOwner = lifecycleOwner
            binding.controller = this
            binding.activeSection = accordionController.activeSection
        }

        override fun onBind() {
            super.onBind()
            binding.model = model
        }

        fun toggleSection() {
            accordionController.activeSection.value =
                model?.id?.takeUnless { it == accordionController.activeSection.value }
        }

        override val contentContainer get() = binding.content
    }
}
