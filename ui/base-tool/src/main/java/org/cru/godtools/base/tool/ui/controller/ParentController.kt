package org.cru.godtools.base.tool.ui.controller

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import kotlin.reflect.KClass
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.xml.model.Content
import org.cru.godtools.xml.model.Event
import org.cru.godtools.xml.model.Parent
import org.greenrobot.eventbus.EventBus

abstract class ParentController<T> protected constructor(
    clazz: KClass<T>,
    root: View,
    parentController: BaseController<*>? = null,
    cacheFactory: UiControllerCache.Factory,
    eventBus: EventBus? = null
) : BaseController<T>(clazz, root, parentController, eventBus) where T : Parent {
    // region Lifecycle
    @CallSuper
    override fun onBind() {
        super.onBind()
        bindContent()
    }

    @CallSuper
    override fun onContentEvent(event: Event) {
        super.onContentEvent(event)
        children?.forEach { it.onContentEvent(event) }
    }

    // XXX: we don't want to short-circuit execution, so we don't use allMatch
    override fun onValidate() = children?.filterNot { it.onValidate() }.isNullOrEmpty()

    @CallSuper
    override fun onBuildEvent(builder: Event.Builder, recursive: Boolean) {
        if (recursive) children?.forEach { it.onBuildEvent(builder, true) }
    }
    // endregion Lifecycle

    // region Child Content
    protected abstract val contentContainer: ViewGroup
    private val childCache by lazy { cacheFactory.create(contentContainer, this) }
    private var children: List<BaseController<Content>>? = null

    protected open val contentToRender get() = model?.content

    @UiThread
    private fun bindContent() {
        children = contentContainer.bindModels(
            models = contentToRender.orEmpty(),
            existing = children.orEmpty().toMutableList(),
            acquireController = { childCache.acquire(it) },
            releaseController = { it.releaseTo(childCache) }
        )
    }
    // endregion Child Content
}
