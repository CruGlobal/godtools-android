package org.cru.godtools.tract.ui.controller

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import androidx.lifecycle.Observer
import com.annimon.stream.Stream
import org.cru.godtools.base.model.Event
import org.cru.godtools.tract.analytics.model.ContentAnalyticsActionEvent
import org.cru.godtools.xml.model.AnalyticsEvent
import org.cru.godtools.xml.model.Base
import org.cru.godtools.xml.model.layoutDirection
import org.greenrobot.eventbus.EventBus
import kotlin.reflect.KClass

@UiThread
abstract class BaseController<T : Base> protected constructor(
    private val modelClass: KClass<T>,
    internal val root: View,
    private val parentController: BaseController<*>? = null
) : Observer<T?> {
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

    open fun onContentEvent(event: Event) = Unit

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
        ids.forEach { EventBus.getDefault().post(builder.id(it).build()) }
    }

    protected fun triggerAnalyticsEvents(events: Collection<AnalyticsEvent>?, vararg types: AnalyticsEvent.Trigger) =
        events?.filter { it.isTriggerType(*types) }?.mapNotNull { sendAnalyticsEvent(it) }.orEmpty()

    private val handler: Handler = Handler(Looper.getMainLooper())
    private fun sendAnalyticsEvent(event: AnalyticsEvent): Runnable? {
        if (event.delay > 0) {
            val task = Runnable {
                EventBus.getDefault().post(ContentAnalyticsActionEvent(event))
            }
            handler.postDelayed(task, event.delay * 1000.toLong())
            return task
        }
        EventBus.getDefault().post(ContentAnalyticsActionEvent(event))
        return null
    }

    protected fun cancelPendingAnalyticsEvents(pendingTasks: List<Runnable>) {
        Stream.of(pendingTasks)
            .forEach { r: Runnable? ->
                handler.removeCallbacks(r)
            }
    }

    /**
     * @return true if the event has been built by a parent controller.
     */
    protected open fun buildEvent(builder: Event.Builder): Boolean = parentController?.buildEvent(builder) == true

    protected open fun validate(ids: Set<Event.Id>): Boolean {
        // navigate up hierarchy before performing validation
        return parentController?.validate(ids) != false
    }
}
