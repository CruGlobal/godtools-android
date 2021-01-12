package org.cru.godtools.tract.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import org.cru.godtools.tract.ui.controller.BaseController
import org.cru.godtools.tract.ui.controller.FallbackController
import org.cru.godtools.tract.ui.controller.ParagraphController
import org.cru.godtools.tract.ui.controller.TextController
import org.cru.godtools.xml.model.Fallback
import org.cru.godtools.xml.model.Paragraph
import org.cru.godtools.xml.model.Text

@Module
@InstallIn(SingletonComponent::class)
abstract class UiControllerModule {
    @Binds
    @IntoMap
    @ContentKey(Fallback::class)
    internal abstract fun fallbackControllerFactory(factory: FallbackController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @ContentKey(Paragraph::class)
    internal abstract fun paragraphControllerFactory(factory: ParagraphController.Factory): BaseController.Factory<*>

    @Binds
    @IntoMap
    @ContentKey(Text::class)
    internal abstract fun textControllerFactory(factory: TextController.Factory): BaseController.Factory<*>
}
