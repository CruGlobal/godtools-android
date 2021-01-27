package org.cru.godtools.base.tool.ui.controller.cache

import android.view.ViewGroup
import androidx.core.util.Pools
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlin.reflect.KClass
import org.ccci.gto.android.common.app.ApplicationUtils
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.xml.model.Base
import timber.log.Timber

class UiControllerCache @AssistedInject internal constructor(
    @Assisted private val parent: ViewGroup,
    @Assisted private val parentController: BaseController<*>,
    private val controllerFactories: Map<UiControllerType, @JvmSuppressWildcards BaseController.Factory<*>>
) {
    @AssistedFactory
    fun interface Factory {
        fun create(parent: ViewGroup, parentController: BaseController<*>): UiControllerCache
    }

    private val pools = mutableMapOf<UiControllerType, Pools.Pool<BaseController<*>>>()
    private val UiControllerType.pool get() = pools.getOrPut(this) { Pools.SimplePool(5) }

    fun <T : Base> acquire(clazz: KClass<T>) =
        (clazz.uiControllerType().pool.acquire() ?: clazz.uiControllerType().createController()) as? BaseController<T>
    fun <T : Base> release(clazz: KClass<T>, instance: BaseController<T>) {
        instance.model = null
        clazz.uiControllerType().pool.release(instance)
    }

    private fun UiControllerType.createController() =
        controllerFactories[this]?.create(parent, parentController) ?: run {
            val e = IllegalArgumentException("Unsupported Content type specified: $this")
            if (ApplicationUtils.isDebuggable(parent.context)) throw e
            Timber.e(e, "Unsupported Content type specified: %s", toString())
            null
        }

    private fun KClass<out Base>.uiControllerType() = UiControllerType.create(this)
}
