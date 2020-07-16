package org.cru.godtools.tract.ui.controller

import android.view.View
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import org.cru.godtools.base.model.Event
import org.cru.godtools.xml.model.Content
import org.cru.godtools.xml.model.Parent
import kotlin.reflect.KClass

abstract class ParentController<T> protected constructor(
    clazz: KClass<T>,
    root: View,
    parentController: BaseController<*>?
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
    protected abstract val contentContainer: LinearLayout
    private val childCache by lazy { UiControllerCache(contentContainer, this) }
    private var children: List<BaseController<Content>>? = null

    @UiThread
    @OptIn(ExperimentalStdlibApi::class)
    private fun bindContent() {
        val existing = children.orEmpty().toMutableList()

        var next: BaseController<Content>? = null
        children = model?.content?.mapNotNull { model ->
            if (next == null) next = existing.removeFirstOrNull()

            (next?.takeIf { it.supportsModel(model) }?.also { next = null }
                ?: childCache.acquire(model.javaClass.kotlin)?.apply {
                    contentContainer.addView(root, contentContainer.indexOfChild(next?.root))
                })?.also { it.model = model }
        }

        next?.let { existing.add(it) }
        existing.forEach {
            contentContainer.removeView(it.root)
            it.releaseTo(childCache)
        }
    }
    // endregion Child Content
}
