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
import org.cru.godtools.base.model.Event
import org.cru.godtools.base.tool.analytics.model.ContentAnalyticsActionEvent
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.model.TrainingTip
import org.cru.godtools.xml.model.AnalyticsEvent
import org.cru.godtools.xml.model.Base
import org.cru.godtools.xml.model.layoutDirection
import org.cru.godtools.xml.model.tips.Tip
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.GodToolsDao

abstract class BaseController<T : Base> protected constructor(
    private val modelClass: KClass<T>,
    val root: View,
    private val parentController: BaseController<*>? = null
) : Observer<T?> {
    interface Factory<U : BaseController<*>> {
        fun create(parent: ViewGroup, parentController: BaseController<*>): U
    }

    protected open val dao: GodToolsDao
        get() {
            checkNotNull(parentController) { "No GodToolsDao found in controller ancestors" }
            return parentController.dao
        }
    @VisibleForTesting(otherwise = PROTECTED)
    open val eventBus: EventBus
        get() {
            checkNotNull(parentController) { "No EventBus found in controller ancestors" }
            return parentController.eventBus
        }
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

    fun supportsModel(model: Base) = modelClass.isInstance(model)
    internal fun releaseTo(cache: UiControllerCache) = model?.let { cache.release(it, this) }

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
    open val isTipsEnabled: Boolean get() = parentController?.isTipsEnabled ?: false

    open fun showTip(tip: Tip?) {
        parentController?.showTip(tip)
    }

    protected fun isTipComplete(tipId: String?): LiveData<Boolean> {
        val manifest = model?.manifest
        return when {
            manifest == null || tipId == null -> ImmutableLiveData(false)
            else -> dao.findLiveData<TrainingTip>(manifest.code, manifest.locale, tipId).map { it?.isCompleted == true }
                .distinctUntilChanged()
        }
    }
    // endregion Tips
}
