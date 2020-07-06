package org.cru.godtools.tract.ui.controller

import android.view.View
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import org.cru.godtools.base.model.Event
import org.cru.godtools.tract.viewmodel.BaseViewHolder
import org.cru.godtools.xml.model.Base
import org.cru.godtools.xml.model.Content
import org.cru.godtools.xml.model.Parent
import kotlin.reflect.KClass

abstract class ParentController<T> : BaseViewHolder<T> where T : Base, T : Parent {
    protected constructor(clazz: KClass<T>, root: View, parentViewHolder: BaseViewHolder<*>?) :
        super(clazz.java, root, parentViewHolder)

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
    private var children: List<BaseViewHolder<Content>>? = null

    @UiThread
    @OptIn(ExperimentalStdlibApi::class)
    private fun bindContent() {
        val existing = children.orEmpty().toMutableList()

        var next: BaseViewHolder<Content>? = null
        children = model?.content?.mapNotNull { model ->
            if (next == null) next = existing.removeFirstOrNull()

            (next?.takeIf { it.supportsModel(model) }?.also { next = null }
                ?: childCache.acquire(model.javaClass.kotlin)?.apply {
                    contentContainer.addView(mRoot, contentContainer.indexOfChild(next?.mRoot))
                })?.apply { bind(model) }
        }

        next?.let { existing.add(it) }
        existing.forEach {
            contentContainer.removeView(it.mRoot)
            it.releaseTo(childCache)
        }
    }
    // endregion Child Content
}
