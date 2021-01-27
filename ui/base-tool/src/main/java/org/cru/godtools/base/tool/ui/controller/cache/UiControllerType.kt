package org.cru.godtools.base.tool.ui.controller.cache

import dagger.MapKey
import kotlin.reflect.KClass
import org.cru.godtools.xml.model.Base

@MapKey(unwrapValue = false)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class UiControllerType(val value: KClass<out Base>) {
    companion object {
        fun create(value: KClass<out Base>) = UiControllerTypeCreator.createUiControllerType(value.java)
    }
}
