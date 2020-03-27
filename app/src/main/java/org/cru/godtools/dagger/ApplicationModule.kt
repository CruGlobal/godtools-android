package org.cru.godtools.dagger

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import org.ccci.gto.android.common.dagger.eager.EagerModule
import org.cru.godtools.analytics.dagger.AnalyticsModule

@Module(includes = [AnalyticsModule::class, EagerModule::class, ServicesModule::class])
class ApplicationModule(@get:Provides val app: Application) {
    @get:Provides
    val context: Context get() = app
}
