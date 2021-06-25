package org.cru.godtools.base.tool.ui.controller.cache

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds
import org.cru.godtools.base.tool.ui.controller.AccordionController
import org.cru.godtools.base.tool.ui.controller.AnimationController
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.base.tool.ui.controller.ContainedButtonController
import org.cru.godtools.base.tool.ui.controller.FallbackController
import org.cru.godtools.base.tool.ui.controller.ImageController
import org.cru.godtools.base.tool.ui.controller.LinkController
import org.cru.godtools.base.tool.ui.controller.OutlinedButtonController
import org.cru.godtools.base.tool.ui.controller.ParagraphController
import org.cru.godtools.base.tool.ui.controller.SpacerController
import org.cru.godtools.base.tool.ui.controller.TabsController
import org.cru.godtools.base.tool.ui.controller.TextController
import org.cru.godtools.base.tool.ui.controller.VideoController
import org.cru.godtools.tool.model.Accordion
import org.cru.godtools.tool.model.Animation
import org.cru.godtools.tool.model.Button
import org.cru.godtools.tool.model.Fallback
import org.cru.godtools.tool.model.Image
import org.cru.godtools.tool.model.Link
import org.cru.godtools.tool.model.Paragraph
import org.cru.godtools.tool.model.Spacer
import org.cru.godtools.tool.model.Tabs
import org.cru.godtools.tool.model.Text
import org.cru.godtools.tool.model.Video

private const val VARIATION_BUTTON_CONTAINED = 1
private const val VARIATION_BUTTON_OUTLINED = 2

@Module
@InstallIn(SingletonComponent::class)
abstract class UiControllerModule {
    @Multibinds
    abstract fun variationResolvers(): Set<VariationResolver>

    @Binds
    @IntoMap
    @UiControllerType(Accordion::class)
    internal abstract fun accordionControllerFactory(factory: AccordionController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @UiControllerType(Animation::class)
    internal abstract fun animationControllerFactory(factory: AnimationController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @UiControllerType(Button::class, VARIATION_BUTTON_CONTAINED)
    internal abstract fun containedButtonFactory(factory: ContainedButtonController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @UiControllerType(Button::class, VARIATION_BUTTON_OUTLINED)
    internal abstract fun outlinedButtonFactory(factory: OutlinedButtonController.Factory): BaseController.Factory<*>

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
    @UiControllerType(Spacer::class)
    internal abstract fun spacerControllerFactory(factory: SpacerController.Factory): BaseController.Factory<*>

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

    companion object {
        @Provides
        @Reusable
        @IntoSet
        fun buttonVariationResolver() = VariationResolver {
            when ((it as? Button)?.style) {
                Button.Style.CONTAINED -> VARIATION_BUTTON_CONTAINED
                Button.Style.OUTLINED -> VARIATION_BUTTON_OUTLINED
                else -> null
            }
        }
    }
}
