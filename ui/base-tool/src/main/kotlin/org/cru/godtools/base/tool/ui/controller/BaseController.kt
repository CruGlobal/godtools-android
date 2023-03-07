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
import kotlin.reflect.KClass
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.cru.godtools.analytics.model.ExitLinkActionEvent
import org.cru.godtools.base.tool.analytics.model.ContentAnalyticsEventAnalyticsActionEvent
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.base.tool.ui.util.layoutDirection
import org.cru.godtools.base.ui.util.openUrl
import org.cru.godtools.db.repository.TrainingTipsRepository
import org.cru.godtools.shared.tool.parser.model.AnalyticsEvent
import org.cru.godtools.shared.tool.parser.model.AnalyticsEvent.Trigger
import org.cru.godtools.shared.tool.parser.model.Base
import org.cru.godtools.shared.tool.parser.model.Clickable
import org.cru.godtools.shared.tool.parser.model.EventId
import org.cru.godtools.shared.tool.parser.model.HasAnalyticsEvents
import org.cru.godtools.shared.tool.parser.model.tips.Tip
import org.cru.godtools.shared.tool.state.State
import org.greenrobot.eventbus.EventBus

abstract class BaseController<T : Base> protected constructor(
    private val modelClass: KClass<T>,
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

    open val lifecycleOwner: LifecycleOwner? get() = parentController?.lifecycleOwner
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open val toolState: State get() = checkNotNull(parentController?.toolState)

    var model: T? = null
        set(value) {
            field = value
            onBind()
        }

    // region Lifecycle
    override fun onChanged(t: T?) {
        model = t
    }

    @CallSuper
    protected open fun onBind() {
        updateLayoutDirection()
    }

    open fun onValidate() = true
    open fun onBuildEvent(builder: Event.Builder, recursive: Boolean) = Unit
    open fun onContentEvent(event: Event) = Unit
    // endregion Lifecycle

    open fun supportsModel(model: Base) = modelClass.isInstance(model)
    internal fun releaseTo(cache: UiControllerCache) = model?.let { cache.release(it, this) }

    // region AnalyticsEvents
    protected fun triggerAnalyticsEvents(events: List<AnalyticsEvent>?) =
        events.orEmpty().mapNotNull { sendAnalyticsEvent(it) }

    private fun sendAnalyticsEvent(event: AnalyticsEvent) = lifecycleOwner?.lifecycleScope?.launch {
        if (event.delay > 0) delay(event.delay * 1000L)
        if (!event.shouldTrigger(toolState)) return@launch
        eventBus.post(ContentAnalyticsEventAnalyticsActionEvent(event))
        event.recordTriggered(toolState)
    }?.takeUnless { it.isCompleted }

    protected fun List<Job>.cancelPendingAnalyticsEvents() = forEach { it.cancel() }
    // endregion AnalyticsEvents

    // region Content Events
    fun sendEvents(ids: List<EventId>?) {
        if (ids.isNullOrEmpty()) return
        if (!validate(ids)) return

        // build the event by walking up the controller hierarchy first in case a parent controller wants to build the
        // event. If a parent controller doesn't build the event, then we populate the event with our own local state.
        val builder = Event.Builder(model?.manifest)
        if (!buildEvent(builder)) onBuildEvent(builder, false)

        // trigger an event for every id provided
        ids.flatMap { it.resolve(toolState) }.forEach { eventBus.post(builder.id(it).build()) }
    }

    /**
     * @return true if the event has been built by a parent controller.
     */
    protected open fun buildEvent(builder: Event.Builder): Boolean = parentController?.buildEvent(builder) == true

    protected open fun validate(ids: List<EventId>): Boolean {
        // navigate up hierarchy before performing validation
        return parentController?.validate(ids) != false
    }
    // endregion Content Events

    // region UI
    /**
     * This method provides a mechanism for binding arbitrary model controllers to a ViewGroup
     */
    protected fun <T : Base, C : BaseController<T>> ViewGroup.bindModels(
        models: List<T>,
        existing: MutableList<C>,
        releaseController: ((controller: C) -> Unit)? = null,
        acquireController: (model: T) -> C?
    ): List<C> {
        var next: C? = null
        try {
            return models.mapNotNull { model ->
                if (next == null) next = existing.removeFirstOrNull()

                val child = next?.takeIf { it.supportsModel(model) }?.also { next = null }
                    ?: acquireController(model)?.also { addView(it.root, indexOfChild(next?.root)) }
                child?.model = model
                child
            }
        } finally {
            next?.let { existing.add(it) }
            existing.forEach {
                removeView(it.root)
                releaseController?.invoke(it)
            }
        }
    }

    protected open fun updateLayoutDirection() {
        // HACK: In theory we should be able to set this on the root page only.
        //       But updating the direction doesn't seem to trigger a re-layout of descendant views.
        root.layoutDirection = model.layoutDirection
    }

    // region Clickable
    open val isClickable get() = (model as? Clickable)?.isClickable ?: false
    val isAncestorClickable: Boolean get() = parentController?.let { it.isClickable || it.isAncestorClickable } ?: false

    fun click(model: Clickable?) {
        if (model == null) return
        if (model is HasAnalyticsEvents) triggerAnalyticsEvents(model.getAnalyticsEvents(Trigger.CLICKED))
        sendEvents(model.events)
        model.url?.let { url ->
            val manifest = model.manifest
            eventBus.post(ExitLinkActionEvent(manifest.code, url, manifest.locale))
            root.context.openUrl(url)
        }
    }
    // endregion Clickable

    // region Tips
    open val enableTips: LiveData<Boolean> get() = parentController?.enableTips ?: ImmutableLiveData(false)

    open fun showTip(tip: Tip?) {
        parentController?.showTip(tip)
    }

    protected fun TrainingTipsRepository.isTipComplete(tipId: String?): LiveData<Boolean> {
        val manifest = model?.manifest
        val tool = manifest?.code
        val locale = manifest?.locale
        return when {
            tool == null || locale == null || tipId == null -> ImmutableLiveData(false)
            else -> isTipCompleteFlow(tool, locale, tipId).distinctUntilChanged().asLiveData()
        }
    }
    // endregion Tips
    // endregion UI
}
