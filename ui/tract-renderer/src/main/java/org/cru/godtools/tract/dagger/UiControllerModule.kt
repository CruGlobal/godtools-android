package org.cru.godtools.tract.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.base.tool.ui.controller.ContentKey
import org.cru.godtools.tract.ui.controller.AnimationController
import org.cru.godtools.tract.ui.controller.ButtonController
import org.cru.godtools.tract.ui.controller.FallbackController
import org.cru.godtools.tract.ui.controller.FormController
import org.cru.godtools.tract.ui.controller.ImageController
import org.cru.godtools.tract.ui.controller.InputController
import org.cru.godtools.tract.ui.controller.ParagraphController
import org.cru.godtools.tract.ui.controller.TabsController
import org.cru.godtools.tract.ui.controller.VideoController
import org.cru.godtools.tract.ui.controller.tips.InlineTipController
import org.cru.godtools.xml.model.Animation
import org.cru.godtools.xml.model.Button
import org.cru.godtools.xml.model.Fallback
import org.cru.godtools.xml.model.Form
import org.cru.godtools.xml.model.Image
import org.cru.godtools.xml.model.Input
import org.cru.godtools.xml.model.Paragraph
import org.cru.godtools.xml.model.Tabs
import org.cru.godtools.xml.model.Video
import org.cru.godtools.xml.model.tips.InlineTip

@Module
@InstallIn(SingletonComponent::class)
abstract class UiControllerModule {
    @Binds
    @IntoMap
    @ContentKey(Animation::class)
    internal abstract fun animationControllerFactory(factory: AnimationController.Factory): BaseController.Factory<*>

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
    @ContentKey(Form::class)
    internal abstract fun formControllerFactory(factory: FormController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @ContentKey(Image::class)
    internal abstract fun imageControllerFactory(factory: ImageController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @ContentKey(InlineTip::class)
    internal abstract fun inlineTipControllerFactory(factory: InlineTipController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @ContentKey(Input::class)
    internal abstract fun inputControllerFactory(factory: InputController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @ContentKey(Paragraph::class)
    internal abstract fun paragraphControllerFactory(factory: ParagraphController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @ContentKey(Tabs::class)
    internal abstract fun tabsControllerFactory(factory: TabsController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @ContentKey(Video::class)
    internal abstract fun videoControllerFactory(factory: VideoController.Factory): BaseController.Factory<*>
}
