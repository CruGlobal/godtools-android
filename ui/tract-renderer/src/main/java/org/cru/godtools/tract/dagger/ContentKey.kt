package org.cru.godtools.tract.dagger

import dagger.MapKey
import kotlin.reflect.KClass
import org.cru.godtools.xml.model.Base

@MapKey
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ContentKey(val value: KClass<out Base>)
