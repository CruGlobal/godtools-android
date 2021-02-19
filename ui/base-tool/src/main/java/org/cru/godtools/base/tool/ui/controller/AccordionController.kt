package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.databinding.ToolContentAccordionBinding
import org.cru.godtools.base.tool.databinding.ToolContentAccordionSectionBinding
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.xml.model.Accordion

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

    private var sectionControllers = emptyList<SectionController>()

    private fun bindSections() {
        sectionControllers = binding.sections.bindModels(
            model?.sections.orEmpty(),
            sectionControllers.toMutableList(),
            acquireController = { sectionFactory.create(binding.sections, this) }
        )
    }

    class SectionController private constructor(
        private val binding: ToolContentAccordionSectionBinding,
        accordionController: AccordionController,
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

        override fun onBind() {
            super.onBind()
            binding.model = model
            binding.selected = true
        }

        override val contentContainer get() = binding.content
    }
}
