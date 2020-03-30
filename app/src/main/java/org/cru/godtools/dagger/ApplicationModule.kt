package org.cru.godtools.dagger

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides

@Module(includes = [ServicesModule::class])
class ApplicationModule(@get:Provides val app: Application) {
    @get:Provides
    val context: Context get() = app
}
