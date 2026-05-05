package org.cru.godtools.ui.tools

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ToolCardModule {
    @Binds
    abstract fun toolCardPresenter(presenter: DefaultToolCardPresenter): ToolCardPresenter
}
