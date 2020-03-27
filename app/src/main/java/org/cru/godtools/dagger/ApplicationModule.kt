package org.cru.godtools.dagger

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(private val app: Application) {
    @Provides
    fun context(): Context = app
}
