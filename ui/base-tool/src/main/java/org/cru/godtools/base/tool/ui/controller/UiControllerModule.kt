package org.cru.godtools.base.tool.ui.controller

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass
import org.cru.godtools.xml.model.Base
import org.cru.godtools.xml.model.Link
import org.cru.godtools.xml.model.Text

@MapKey
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ContentKey(val value: KClass<out Base>)

@Module
@InstallIn(SingletonComponent::class)
abstract class UiControllerModule {
    @Binds
    @IntoMap
    @ContentKey(Link::class)
    internal abstract fun linkControllerFactory(factory: LinkController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @ContentKey(Text::class)
    internal abstract fun textControllerFactory(factory: TextController.Factory): BaseController.Factory<*>
}
