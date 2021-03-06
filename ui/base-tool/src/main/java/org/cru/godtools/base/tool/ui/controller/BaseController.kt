package org.cru.godtools.base.tool.ui.controller

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PROTECTED
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import kotlin.reflect.KClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.ccci.gto.android.common.db.findLiveData
import org.cru.godtools.base.tool.analytics.model.ContentAnalyticsActionEvent
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.base.tool.ui.util.layoutDirection
import org.cru.godtools.model.TrainingTip
import org.cru.godtools.tool.model.AnalyticsEvent
import org.cru.godtools.tool.model.Base
import org.cru.godtools.tool.model.EventId
import org.cru.godtools.tool.model.tips.Tip
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.GodToolsDao

abstract class BaseController<T : Base> protected constructor(
    private val modelClass: KClass<T>,
    val root: View,
    private val parentController: BaseController<*>? = null,
    eventBus: EventBus? = null
) : Observer<T?> {
    interface Factory<U : BaseController<*>> {
        fun create(parent: ViewGroup, parentController: BaseController<*>): U
    }

    private val _eventBus = eventBus
    @VisibleForTesting(otherwise = PROTECTED)
    val eventBus: EventBus
        get() = _eventBus ?: parentController?.eventBus ?: error("No EventBus found in controller hierarchy")

    open val lifecycleOwner: LifecycleOwner? get() = parentController?.lifecycleOwner

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

    fun sendEvents(ids: List<EventId>?) {
        if (ids.isNullOrEmpty()) return
        if (!validate(ids)) return

        // try letting a parent build the event object
        val builder = Event.Builder().apply {
            tool = model?.manifest?.code
            locale = model?.manifest?.locale
        }

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

    protected open fun validate(ids: List<EventId>): Boolean {
        // navigate up hierarchy before performing validation
        return parentController?.validate(ids) != false
    }

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

    // region Tips
    open val isTipsEnabled: Boolean get() = parentController?.isTipsEnabled ?: false

    open fun showTip(tip: Tip?) {
        parentController?.showTip(tip)
    }

    protected fun GodToolsDao.isTipComplete(tipId: String?): LiveData<Boolean> {
        val manifest = model?.manifest
        val tool = manifest?.code
        val locale = manifest?.locale
        return when {
            tool == null || locale == null || tipId == null -> ImmutableLiveData(false)
            else -> findLiveData<TrainingTip>(tool, locale, tipId).map { it?.isCompleted == true }
                .distinctUntilChanged()
        }
    }
    // endregion Tips
    // endregion UI
}
