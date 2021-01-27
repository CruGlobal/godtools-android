package org.cru.godtools.tract

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerType
import org.cru.godtools.tract.ui.controller.FormController
import org.cru.godtools.tract.ui.controller.InputController
import org.cru.godtools.tract.ui.controller.tips.InlineTipController
import org.cru.godtools.xml.model.Form
import org.cru.godtools.xml.model.Input
import org.cru.godtools.xml.model.tips.InlineTip

@Module
@InstallIn(SingletonComponent::class)
abstract class TractUiControllerModule {
    @Binds
    @IntoMap
    @UiControllerType(Form::class)
    internal abstract fun formControllerFactory(factory: FormController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @UiControllerType(InlineTip::class)
    internal abstract fun inlineTipControllerFactory(factory: InlineTipController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @UiControllerType(Input::class)
    internal abstract fun inputControllerFactory(factory: InputController.Factory): BaseController.Factory<*>
}
