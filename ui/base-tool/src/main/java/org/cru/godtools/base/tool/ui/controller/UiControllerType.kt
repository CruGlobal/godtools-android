package org.cru.godtools.base.tool.ui.controller

import dagger.MapKey
import kotlin.reflect.KClass
import org.cru.godtools.xml.model.Base

@MapKey
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class UiControllerType(val value: KClass<out Base>)
