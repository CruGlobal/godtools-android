package org.cru.godtools.base

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class SettingsModule {
    companion object {
        @Provides
        @Singleton
        internal fun provideSettings(context: Context) = Settings.getInstance(context)
    }
}
