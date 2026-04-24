package org.cru.godtools.base.tool.ui.controller

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import io.fluidsonic.locale.toPlatform
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.cru.godtools.base.tool.analytics.model.ContentAnalyticsEventAnalyticsActionEvent
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.ui.util.layoutDirection
import org.cru.godtools.db.repository.TrainingTipsRepository
import org.cru.godtools.shared.renderer.state.State
import org.cru.godtools.shared.tool.parser.model.AnalyticsEvent
import org.cru.godtools.shared.tool.parser.model.Base
import org.cru.godtools.shared.tool.parser.model.recordTriggered
import org.cru.godtools.shared.tool.parser.model.shouldTrigger
import org.cru.godtools.shared.tool.parser.model.tips.Tip
import org.greenrobot.eventbus.EventBus

abstract class BaseController<T : Base> protected constructor(
    val root: View,
    private val parentController: BaseController<*>? = null,
    eventBus: EventBus? = null
) : Observer<T?> {
    interface Factory<U : BaseController<*>> {
        fun create(parent: ViewGroup, parentController: BaseController<*>): U
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val eventBus: EventBus by lazy {
        eventBus ?: parentController?.eventBus ?: error("No EventBus found in controller hierarchy")
    }

    open val lifecycleOwner: LifecycleOwner
        get() = parentController?.lifecycleOwner ?: error("No LifecycleOwner found in controller hierarchy")
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open val toolState: State get() = checkNotNull(parentController?.toolState)

    var model: T? = null
        set(value) {
            field = value
            onBind()
        }

    // region Lifecycle
    override fun onChanged(value: T?) {
        model = value
    }

    @CallSuper
    protected open fun onBind() {
        updateLayoutDirection()
    }

    open fun onContentEvent(event: Event) = Unit
    // endregion Lifecycle

    // region AnalyticsEvents
    protected fun triggerAnalyticsEvents(events: List<AnalyticsEvent>?) =
        events.orEmpty().mapNotNull { sendAnalyticsEvent(it) }

    private fun sendAnalyticsEvent(event: AnalyticsEvent) = lifecycleOwner.lifecycleScope.launch {
        if (event.delay > 0) delay(event.delay * 1000L)
        if (!event.shouldTrigger(toolState)) return@launch
        eventBus.post(ContentAnalyticsEventAnalyticsActionEvent(event, model?.manifest))
        event.recordTriggered(toolState)
    }.takeUnless { it.isCompleted }

    protected fun List<Job>.cancelPendingAnalyticsEvents() = forEach { it.cancel() }
    // endregion AnalyticsEvents

    // region UI
    protected open fun updateLayoutDirection() {
        // HACK: In theory we should be able to set this on the root page only.
        //       But updating the direction doesn't seem to trigger a re-layout of descendant views.
        root.layoutDirection = model.layoutDirection
    }

    // region Tips
    open val enableTips: LiveData<Boolean> get() = parentController?.enableTips ?: ImmutableLiveData(false)

    open fun showTip(tip: Tip?) {
        parentController?.showTip(tip)
    }

    protected fun TrainingTipsRepository.isTipComplete(tipId: String?): LiveData<Boolean> {
        val manifest = model?.manifest
        val tool = manifest?.code
        val locale = manifest?.locale?.toPlatform()
        return when {
            tool == null || locale == null || tipId == null -> ImmutableLiveData(false)
            else -> isTipCompleteFlow(tool, locale, tipId).distinctUntilChanged().asLiveData()
        }
    }
    // endregion Tips
    // endregion UI
}
