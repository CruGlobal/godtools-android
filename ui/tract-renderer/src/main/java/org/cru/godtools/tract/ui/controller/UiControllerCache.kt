package org.cru.godtools.tract.ui.controller

import android.view.ViewGroup
import androidx.core.util.Pools
import kotlin.reflect.KClass
import org.ccci.gto.android.common.app.ApplicationUtils
import org.cru.godtools.tract.ui.controller.tips.InlineTipController
import org.cru.godtools.xml.model.Base
import org.cru.godtools.xml.model.Button
import org.cru.godtools.xml.model.Fallback
import org.cru.godtools.xml.model.Form
import org.cru.godtools.xml.model.Image
import org.cru.godtools.xml.model.Input
import org.cru.godtools.xml.model.Link
import org.cru.godtools.xml.model.Paragraph
import org.cru.godtools.xml.model.Tabs
import org.cru.godtools.xml.model.Text
import org.cru.godtools.xml.model.Video
import org.cru.godtools.xml.model.tips.InlineTip
import timber.log.Timber

internal class UiControllerCache(private val parent: ViewGroup, private val parentController: BaseController<*>?) {
    private val pools = mutableMapOf<KClass<*>, Pools.Pool<BaseController<*>>>()

    @Suppress("UNCHECKED_CAST")
    private val <T : Base> KClass<T>.pool get() = pools[this] as? Pools.Pool<BaseController<T>>
        ?: Pools.SimplePool<BaseController<T>>(5).also { pools[this] = it as Pools.Pool<BaseController<*>> }

    fun <T : Base> acquire(clazz: KClass<T>) = clazz.pool.acquire() ?: createController(clazz)
    fun <T : Base> release(clazz: KClass<T>, instance: BaseController<T>) {
        instance.model = null
        clazz.pool.release(instance)
    }

    private fun <T : Base> createController(clazz: KClass<T>) = when (clazz) {
        Button::class -> ButtonController(parent, parentController)
        Fallback::class -> FallbackController(parent, parentController)
        Form::class -> FormController(parent, parentController)
        Image::class -> ImageController(parent, parentController)
        InlineTip::class -> InlineTipController(parent, parentController)
        Input::class -> InputController(parent, parentController)
        Link::class -> LinkController(parent, parentController)
        Paragraph::class -> ParagraphController(parent, parentController)
        Tabs::class -> TabsController(parent, parentController)
        Text::class -> TextController(parent, parentController)
        Video::class -> VideoController(parent, parentController)
        else -> {
            val e = IllegalArgumentException("Unsupported Content class specified: ${clazz.simpleName}")
            if (ApplicationUtils.isDebuggable(parent.context)) throw e
            Timber.e(e, "Unsupported Content class specified: %s", clazz.simpleName)
            null
        }
    } as BaseController<T>?
}
