package org.cru.godtools.tract.ui.controller

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import kotlin.reflect.KClass
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.model.Content
import org.cru.godtools.xml.model.Parent

abstract class ParentController<T> protected constructor(
    clazz: KClass<T>,
    root: View,
    parentController: BaseController<*>? = null,
    cacheFactory: UiControllerCache.Factory
) : BaseController<T>(clazz, root, parentController) where T : Parent {
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
    @OptIn(ExperimentalStdlibApi::class)
    private fun bindContent() {
        val existing = children.orEmpty().toMutableList()

        var next: BaseController<Content>? = null
        children = contentToRender?.mapNotNull { model ->
            if (next == null) next = existing.removeFirstOrNull()

            val child = next?.takeIf { it.supportsModel(model) }?.also { next = null }
                ?: childCache.acquire(model.javaClass.kotlin)
                    ?.also { contentContainer.addView(it.root, contentContainer.indexOfChild(next?.root)) }
            child?.model = model
            child
        }

        next?.let { existing.add(it) }
        existing.forEach {
            contentContainer.removeView(it.root)
            it.releaseTo(childCache)
        }
    }
    // endregion Child Content
}
