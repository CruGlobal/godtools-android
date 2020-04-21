package org.cru.godtools.dagger

import dagger.Module
import org.cru.godtools.analytics.DebugAnalyticsModule

@Module(includes = [DebugAnalyticsModule::class, FlipperModule::class])
abstract class DebugServicesModule
