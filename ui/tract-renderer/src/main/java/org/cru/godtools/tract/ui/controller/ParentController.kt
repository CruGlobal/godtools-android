package org.cru.godtools.tract.ui.controller

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import org.cru.godtools.base.model.Event
import org.cru.godtools.tract.viewmodel.BaseViewHolder
import org.cru.godtools.xml.model.Base
import org.cru.godtools.xml.model.Parent
import kotlin.reflect.KClass

abstract class ParentController<T> : BaseViewHolder<T> where T : Base, T : Parent {
    protected constructor(clazz: KClass<T>, root: View, parentViewHolder: BaseViewHolder<*>?) :
        super(clazz.java, root, parentViewHolder)
    protected constructor(clazz: Class<T>, parent: ViewGroup, layout: Int, parentViewHolder: BaseViewHolder<*>?) :
        super(clazz, parent, layout, parentViewHolder)

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
    private var children: List<BaseViewHolder<*>>? = null

    @UiThread
    private fun bindContent() {
        contentContainer.removeAllViews()
        children = model?.content?.mapNotNull {
            createController(it.javaClass.kotlin, contentContainer, this)?.apply {
                bind(it)
                contentContainer.addView(mRoot)
            }
        }
    }
    // endregion Child Content
}
