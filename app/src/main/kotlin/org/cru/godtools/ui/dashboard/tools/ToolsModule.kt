package org.cru.godtools.ui.dashboard.tools

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ToolsModule {
    @Binds
    abstract fun toolFiltersStateProducer(impl: DefaultToolFiltersStateProducer): ToolFiltersStateProducer
}
