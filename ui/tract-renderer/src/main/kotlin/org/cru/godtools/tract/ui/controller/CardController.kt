package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import javax.inject.Named
import kotlinx.coroutines.Job
import okio.FileSystem
import org.ccci.gto.android.common.androidx.lifecycle.ConstrainedStateLifecycleOwner
import org.ccci.gto.android.common.androidx.lifecycle.onPause
import org.ccci.gto.android.common.androidx.lifecycle.onResume
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.TOOL_RESOURCE_FILE_SYSTEM
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.shared.renderer.content.RenderContentStack
import org.cru.godtools.shared.renderer.util.ProvideRendererServices
import org.cru.godtools.shared.tool.parser.model.AnalyticsEvent.Trigger
import org.cru.godtools.shared.tool.parser.model.tract.TractPage.Card
import org.cru.godtools.tool.tract.databinding.TractContentCardBinding

class CardController private constructor(
    private val binding: TractContentCardBinding,
    pageController: PageController,
    private val resourceFileSystem: FileSystem,
) : BaseController<Card>(Card::class, binding.root, pageController) {
    @AssistedInject
    internal constructor(
        @Assisted parent: ViewGroup,
        @Assisted pageController: PageController,
        @Named(TOOL_RESOURCE_FILE_SYSTEM) resourceFileSystem: FileSystem,
    ) : this(
        binding = TractContentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        pageController = pageController,
        resourceFileSystem = resourceFileSystem,
    )

    @AssistedFactory
    interface Factory {
        fun create(parent: ViewGroup, pageController: PageController): CardController
    }

    interface Callbacks {
        fun onToggleCard(controller: CardController)
        fun onPreviousCard()
        fun onNextCard()
        fun onDismissCard(controller: CardController)
    }

    override val lifecycleOwner = ConstrainedStateLifecycleOwner(pageController.lifecycleOwner, Lifecycle.State.CREATED)

    private val callbacks: Callbacks = pageController
    private var pendingAnalyticsEvents: List<Job>? = null

    init {
        binding.lifecycleOwner = lifecycleOwner
        binding.controller = this
        binding.enableTips = pageController.enableTips

        with(lifecycleOwner.lifecycle) {
            onResume { pendingAnalyticsEvents = triggerAnalyticsEvents(model?.getAnalyticsEvents(Trigger.VISIBLE)) }
            onPause { pendingAnalyticsEvents?.cancelPendingAnalyticsEvents() }
        }
    }

    // region Lifecycle
    @CallSuper
    override fun onBind() {
        super.onBind()
        binding.model = model
        binding.compose.setContent {
            ProvideRendererServices(resourceFileSystem) {
                RenderContentStack(
                    model?.content.orEmpty(),
                    state = toolState,
                )
            }
        }
    }

    @CallSuper
    override fun onContentEvent(event: Event) {
        super.onContentEvent(event)
        processDismissEvent(event)
    }
    // endregion Lifecycle

    private fun processDismissEvent(event: Event) {
        if (model?.dismissListeners?.contains(event.id) == true) dismissCard()
    }

    fun toggleCard() = callbacks.onToggleCard(this)
    fun nextCard() = callbacks.onNextCard()
    fun previousCard() = callbacks.onPreviousCard()
    private fun dismissCard() = callbacks.onDismissCard(this)
}
