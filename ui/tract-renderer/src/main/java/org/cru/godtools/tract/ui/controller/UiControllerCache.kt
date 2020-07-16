package org.cru.godtools.tract.ui.controller

import android.view.ViewGroup
import androidx.core.util.Pools
import org.ccci.gto.android.common.app.ApplicationUtils
import org.cru.godtools.xml.model.BaseModel
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

internal class UiControllerCache(private val parent: ViewGroup, private val parentController: BaseController<*>?) {
    private val pools = mutableMapOf<KClass<*>, Pools.Pool<BaseController<*>>>()

    @Suppress("UNCHECKED_CAST")
    private val <T : BaseModel> KClass<T>.pool get() = pools[this] as? Pools.Pool<BaseController<T>>
            ?: Pools.SimplePool<BaseController<T>>(5).also { pools[this] = it as Pools.Pool<BaseController<*>> }

    fun <T : BaseModel> acquire(clazz: KClass<T>): BaseController<T>? =
        clazz.pool.acquire() ?: createController(clazz, parent, parentController)
    fun <T : BaseModel> release(clazz: KClass<T>, instance: BaseController<T>) {
        instance.model = null
        clazz.pool.release(instance)
    }

    private fun <T : BaseModel> createController(
        clazz: KClass<T>,
        parent: ViewGroup,
        parentViewHolder: BaseController<*>?
    ) = when (clazz) {
        Button::class -> ButtonController(parent, parentViewHolder)
        Form::class -> FormController(parent, parentViewHolder)
        Image::class -> ImageController(parent, parentViewHolder)
        Input::class -> InputController(parent, parentViewHolder)
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
    } as BaseController<T>?
}
