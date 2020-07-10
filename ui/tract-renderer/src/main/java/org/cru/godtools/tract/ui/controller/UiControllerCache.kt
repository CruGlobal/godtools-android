package org.cru.godtools.tract.ui.controller

import android.view.ViewGroup
import androidx.core.util.Pools
import org.ccci.gto.android.common.app.ApplicationUtils
import org.cru.godtools.tract.viewmodel.BaseViewHolder
import org.cru.godtools.tract.viewmodel.InputViewHolder
import org.cru.godtools.xml.model.Base
import org.cru.godtools.xml.model.Button
import org.cru.godtools.xml.model.Form
import org.cru.godtools.xml.model.Image
import org.cru.godtools.xml.model.Input
import org.cru.godtools.xml.model.Link
import org.cru.godtools.xml.model.Paragraph
import org.cru.godtools.xml.model.Tabs
import org.cru.godtools.xml.model.Text
import timber.log.Timber
import kotlin.reflect.KClass

internal class UiControllerCache(private val parent: ViewGroup, private val parentViewHolder: BaseViewHolder<*>?) {
    private val pools = mutableMapOf<KClass<*>, Pools.Pool<BaseViewHolder<*>>>()
    @Suppress("UNCHECKED_CAST")
    private val <T : Base> KClass<T>.pool
        get() = pools[this] as? Pools.Pool<BaseViewHolder<T>>
            ?: Pools.SimplePool<BaseViewHolder<T>>(5).also { pools[this] = it as Pools.Pool<BaseViewHolder<*>> }

    fun <T : Base> acquire(clazz: KClass<T>): BaseViewHolder<T>? =
        clazz.pool.acquire() ?: createController(clazz, parent, parentViewHolder)
    fun <T : Base> release(clazz: KClass<T>, instance: BaseViewHolder<T>) {
        instance.bind(null)
        clazz.pool.release(instance)
    }

    private fun <T : Base> createController(clazz: KClass<T>, parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) =
        when (clazz) {
            Button::class -> ButtonController(parent, parentViewHolder)
            Form::class -> FormController(parent, parentViewHolder)
            Image::class -> ImageController(parent, parentViewHolder)
            Input::class -> InputViewHolder(parent, parentViewHolder)
            Link::class -> LinkController(parent, parentViewHolder)
            Paragraph::class -> ParagraphController(parent, parentViewHolder)
            Tabs::class -> TabsController(parent, parentViewHolder)
            Text::class -> TextController(parent, parentViewHolder)
            else -> {
                val e = IllegalArgumentException("Unsupported Content class specified: ${clazz.simpleName}")
                if (ApplicationUtils.isDebuggable(parent.context)) throw e
                Timber.e(e, "Unsupported Content class specified: %s", clazz.simpleName)
                null
            }
        } as BaseViewHolder<T>?

    @Deprecated(
        "Used as a bridge method until BaseViewHolder is converted to Kotlin",
        ReplaceWith("release(clazz.kotlin, instance)")
    )
    fun <T : Base> release(clazz: Class<T>, instance: BaseViewHolder<T>) = release(clazz.kotlin, instance)
}
