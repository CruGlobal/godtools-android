package org.cru.godtools.dagger

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module(includes = [DebugServicesModule::class])
abstract class DebugApplicationModule
