package org.cru.godtools.base.tool.ui.controller

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import kotlin.reflect.KClass
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.model.Content
import org.cru.godtools.tool.model.Parent
import org.greenrobot.eventbus.EventBus

abstract class ParentController<T : Parent> protected constructor(
    clazz: KClass<T>,
    root: View,
    parentController: BaseController<*>? = null,
    cacheFactory: UiControllerCache.Factory,
    eventBus: EventBus? = null
) : BaseController<T>(clazz, root, parentController, eventBus) {
    // region Lifecycle
    @CallSuper
    override fun onBind() {
        super.onBind()
        bindChildren()
    }

    @CallSuper
    override fun onContentEvent(event: Event) {
        super.onContentEvent(event)
        propagateContentEventToChildren(event)
    }

    override fun onValidate() = validateChildren()

    @CallSuper
    override fun onBuildEvent(builder: Event.Builder, recursive: Boolean) {
        if (recursive) childControllers.forEach { it.onBuildEvent(builder, true) }
    }
    // endregion Lifecycle

    // region Children
    protected abstract val childContainer: ViewGroup
    private val childCache by lazy { cacheFactory.create(childContainer, this) }
    private var childControllers = emptyList<BaseController<Content>>()

    protected open val childrenToRender get() = model?.content.orEmpty()

    @UiThread
    private fun bindChildren() {
        childControllers = childContainer.bindModels(
            models = childrenToRender,
            existing = childControllers.toMutableList(),
            releaseController = { it.releaseTo(childCache) }
        ) { childCache.acquire(it) }
    }

    private fun propagateContentEventToChildren(event: Event) = childControllers.forEach { it.onContentEvent(event) }

    // XXX: we don't want to short-circuit execution, so we don't use allMatch
    private fun validateChildren() = childControllers.filterNot { it.onValidate() }.isEmpty()
    // endregion Children
}
