package org.cru.godtools.tool.tips

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerType
import org.cru.godtools.tool.model.tips.InlineTip
import org.cru.godtools.tool.tips.ui.controller.InlineTipController

@Module
@InstallIn(SingletonComponent::class)
abstract class TipsModule {
    @Binds
    @IntoMap
    @UiControllerType(InlineTip::class)
    internal abstract fun inlineTipControllerFactory(factory: InlineTipController.Factory): BaseController.Factory<*>
}
