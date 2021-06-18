package org.cru.godtools.base.tool.ui.controller.cache

import dagger.MapKey
import kotlin.reflect.KClass
import org.cru.godtools.tool.model.Base

@MapKey(unwrapValue = false)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class UiControllerType(val value: KClass<out Base>, val variation: Int = DEFAULT_VARIATION) {
    companion object {
        const val DEFAULT_VARIATION = 0

        fun create(value: KClass<out Base>, variation: Int = DEFAULT_VARIATION) =
            UiControllerTypeCreator.createUiControllerType(value.java, variation)
    }
}
