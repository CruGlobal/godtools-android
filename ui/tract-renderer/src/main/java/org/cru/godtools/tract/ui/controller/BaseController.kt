package org.cru.godtools.tract.ui.controller

import android.view.View
import androidx.annotation.CallSuper
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cru.godtools.base.model.Event
import org.cru.godtools.tract.analytics.model.ContentAnalyticsActionEvent
import org.cru.godtools.xml.model.AnalyticsEvent
import org.cru.godtools.xml.model.Base
import org.cru.godtools.xml.model.layoutDirection
import org.cru.godtools.xml.model.tips.Tip
import org.greenrobot.eventbus.EventBus
import kotlin.reflect.KClass

abstract class BaseController<T : Base> protected constructor(
    private val modelClass: KClass<T>,
    internal val root: View,
    private val parentController: BaseController<*>? = null
) : Observer<T?> {
    protected open val eventBus: EventBus
        get() {
            checkNotNull(parentController) { "No EventBus found in controller ancestors" }
            return parentController.eventBus
        }

    var model: T? = null
        set(value) {
            if (value == null) isVisible = false
            field = value
            onBind()
        }

    var isVisible = false
        set(value) {
            if (field == value) return
            field = value
            if (field) onVisible() else onHidden()
        }

    // region Lifecycle
    override fun onChanged(t: T?) {
        model = t
    }

    @CallSuper
    protected open fun onBind() {
        updateLayoutDirection()
    }

    protected open fun onVisible() = Unit
    internal open fun onValidate() = true
    internal open fun onBuildEvent(builder: Event.Builder, recursive: Boolean) = Unit
    internal open fun onContentEvent(event: Event) = Unit
    protected open fun onHidden() = Unit
    // endregion Lifecycle

    fun supportsModel(model: Base?) = modelClass.isInstance(model)
    internal fun releaseTo(cache: UiControllerCache) = cache.release(modelClass, this)

    protected open fun updateLayoutDirection() {
        // HACK: In theory we should be able to set this on the root page only.
        // HACK: But updating the direction doesn't seem to trigger a re-layout of descendant views.
        root.layoutDirection = model.layoutDirection
    }

    fun sendEvents(ids: Set<Event.Id>?) {
        if (ids.isNullOrEmpty()) return
        if (!validate(ids)) return

        // try letting a parent build the event object
        val builder = Event.builder()
        model?.manifest?.locale?.let { builder.locale(it) }

        // populate the event with our local state if it wasn't populated by a parent
        if (!buildEvent(builder)) onBuildEvent(builder, false)

        // trigger an event for every id provided
        ids.forEach { eventBus.post(builder.id(it).build()) }
    }

    protected fun triggerAnalyticsEvents(events: Collection<AnalyticsEvent>?, vararg types: AnalyticsEvent.Trigger) =
        events?.filter { it.isTriggerType(*types) }?.mapNotNull { sendAnalyticsEvent(it) }.orEmpty()

    private fun sendAnalyticsEvent(event: AnalyticsEvent) = GlobalScope.launch(Dispatchers.Main.immediate) {
        if (event.delay > 0) delay(event.delay * 1000L)
        eventBus.post(ContentAnalyticsActionEvent(event))
    }.takeUnless { it.isCompleted }

    protected fun List<Job>.cancelPendingAnalyticsEvents() = forEach { it.cancel() }

    /**
     * @return true if the event has been built by a parent controller.
     */
    protected open fun buildEvent(builder: Event.Builder): Boolean = parentController?.buildEvent(builder) == true

    protected open fun validate(ids: Set<Event.Id>): Boolean {
        // navigate up hierarchy before performing validation
        return parentController?.validate(ids) != false
    }

    // region Tips
    internal open val isTipsEnabled: Boolean get() = parentController?.isTipsEnabled ?: false

    open fun showTip(tip: Tip?) {
        parentController?.showTip(tip)
    }
    // endregion Tips
}
