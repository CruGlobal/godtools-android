package org.cru.godtools.tract.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import org.cru.godtools.tract.ui.controller.AnimationController
import org.cru.godtools.tract.ui.controller.BaseController
import org.cru.godtools.tract.ui.controller.ButtonController
import org.cru.godtools.tract.ui.controller.FallbackController
import org.cru.godtools.tract.ui.controller.FormController
import org.cru.godtools.tract.ui.controller.ImageController
import org.cru.godtools.tract.ui.controller.ParagraphController
import org.cru.godtools.tract.ui.controller.TabsController
import org.cru.godtools.tract.ui.controller.TextController
import org.cru.godtools.xml.model.Animation
import org.cru.godtools.xml.model.Button
import org.cru.godtools.xml.model.Fallback
import org.cru.godtools.xml.model.Form
import org.cru.godtools.xml.model.Image
import org.cru.godtools.xml.model.Paragraph
import org.cru.godtools.xml.model.Tabs
import org.cru.godtools.xml.model.Text

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
    @ContentKey(Paragraph::class)
    internal abstract fun paragraphControllerFactory(factory: ParagraphController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @ContentKey(Tabs::class)
    internal abstract fun tabsControllerFactory(factory: TabsController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @ContentKey(Text::class)
    internal abstract fun textControllerFactory(factory: TextController.Factory): BaseController.Factory<*>
}
