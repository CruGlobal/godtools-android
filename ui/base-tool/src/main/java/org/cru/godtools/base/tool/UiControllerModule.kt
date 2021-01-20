package org.cru.godtools.base.tool

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import org.cru.godtools.base.tool.ui.controller.AnimationController
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.base.tool.ui.controller.ButtonController
import org.cru.godtools.base.tool.ui.controller.FallbackController
import org.cru.godtools.base.tool.ui.controller.ImageController
import org.cru.godtools.base.tool.ui.controller.LinkController
import org.cru.godtools.base.tool.ui.controller.ParagraphController
import org.cru.godtools.base.tool.ui.controller.TabsController
import org.cru.godtools.base.tool.ui.controller.TextController
import org.cru.godtools.base.tool.ui.controller.UiControllerType
import org.cru.godtools.base.tool.ui.controller.VideoController
import org.cru.godtools.xml.model.Animation
import org.cru.godtools.xml.model.Button
import org.cru.godtools.xml.model.Fallback
import org.cru.godtools.xml.model.Image
import org.cru.godtools.xml.model.Link
import org.cru.godtools.xml.model.Paragraph
import org.cru.godtools.xml.model.Tabs
import org.cru.godtools.xml.model.Text
import org.cru.godtools.xml.model.Video

@Module
@InstallIn(SingletonComponent::class)
abstract class UiControllerModule {
    @Binds
    @IntoMap
    @UiControllerType(Animation::class)
    internal abstract fun animationControllerFactory(factory: AnimationController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @UiControllerType(Button::class)
    internal abstract fun buttonControllerFactory(factory: ButtonController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @UiControllerType(Fallback::class)
    internal abstract fun fallbackControllerFactory(factory: FallbackController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @UiControllerType(Image::class)
    internal abstract fun imageControllerFactory(factory: ImageController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @UiControllerType(Link::class)
    internal abstract fun linkControllerFactory(factory: LinkController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @UiControllerType(Paragraph::class)
    internal abstract fun paragraphControllerFactory(factory: ParagraphController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @UiControllerType(Tabs::class)
    internal abstract fun tabsControllerFactory(factory: TabsController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @UiControllerType(Text::class)
    internal abstract fun textControllerFactory(factory: TextController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @UiControllerType(Video::class)
    internal abstract fun videoControllerFactory(factory: VideoController.Factory): BaseController.Factory<*>
}
