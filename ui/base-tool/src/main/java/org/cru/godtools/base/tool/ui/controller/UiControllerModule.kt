package org.cru.godtools.base.tool.ui.controller

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass
import org.cru.godtools.xml.model.Base
import org.cru.godtools.xml.model.Button
import org.cru.godtools.xml.model.Fallback
import org.cru.godtools.xml.model.Link
import org.cru.godtools.xml.model.Tabs
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
    @ContentKey(Button::class)
    internal abstract fun buttonControllerFactory(factory: ButtonController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @ContentKey(Fallback::class)
    internal abstract fun fallbackControllerFactory(factory: FallbackController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @ContentKey(Link::class)
    internal abstract fun linkControllerFactory(factory: LinkController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @ContentKey(Tabs::class)
    internal abstract fun tabsControllerFactory(factory: TabsController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @ContentKey(Text::class)
    internal abstract fun textControllerFactory(factory: TextController.Factory): BaseController.Factory<*>
}
