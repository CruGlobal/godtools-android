package org.cru.godtools.base.tool.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import org.ccci.gto.android.common.androidx.lifecycle.ConstrainedStateLifecycleOwner
import org.ccci.gto.android.common.androidx.lifecycle.onPause
import org.ccci.gto.android.common.androidx.lifecycle.onResume
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.shared.tool.parser.model.Accordion
import org.cru.godtools.shared.tool.parser.model.AnalyticsEvent.Trigger
import org.cru.godtools.tool.databinding.ToolContentAccordionBinding
import org.cru.godtools.tool.databinding.ToolContentAccordionSectionBinding

class AccordionController @VisibleForTesting internal constructor(
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
            acquireController = { sectionFactory.create(binding.sections, this) },
            releaseController = { it.lifecycleOwner?.maxState = Lifecycle.State.DESTROYED }
        )
    }
    // endregion Sections

    class SectionController @VisibleForTesting internal constructor(
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

        private var pendingVisibleAnalyticsEvents: List<Job>? = null

        override val lifecycleOwner =
            ConstrainedStateLifecycleOwner(accordionController.lifecycleOwner, Lifecycle.State.CREATED)

        init {
            binding.lifecycleOwner = lifecycleOwner
            binding.controller = this
            binding.activeSection = accordionController.activeSection

            accordionController.activeSection.observe(accordionController.lifecycleOwner) { updateLifecycleMaxState() }

            with(lifecycleOwner.lifecycle) {
                onResume {
                    pendingVisibleAnalyticsEvents = triggerAnalyticsEvents(model?.getAnalyticsEvents(Trigger.VISIBLE))
                }
                onPause { pendingVisibleAnalyticsEvents?.cancelPendingAnalyticsEvents() }
            }
        }

        override fun onBind() {
            updateLifecycleMaxState()
            super.onBind()
            binding.model = model
        }

        fun toggleSection() {
            accordionController.activeSection.value =
                model?.id?.takeUnless { it == accordionController.activeSection.value }
        }

        private fun updateLifecycleMaxState() {
            lifecycleOwner.maxState = when {
                accordionController.isActiveSection(model) -> Lifecycle.State.RESUMED
                model != null -> Lifecycle.State.STARTED
                else -> Lifecycle.State.CREATED
            }
        }

        override val childContainer get() = binding.content
    }

    @VisibleForTesting
    internal fun isActiveSection(section: Accordion.Section?) =
        section?.id.let { it != null && it == activeSection.value }
}
