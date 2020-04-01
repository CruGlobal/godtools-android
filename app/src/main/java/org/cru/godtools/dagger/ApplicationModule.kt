package org.cru.godtools.dagger

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import org.cru.godtools.ui.UiModule

@Module(includes = [ServicesModule::class, UiModule::class])
class ApplicationModule(@get:Provides val app: Application) {
    @get:Provides
    val context: Context get() = app
}
