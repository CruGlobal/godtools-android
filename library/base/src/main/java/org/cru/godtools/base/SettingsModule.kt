package org.cru.godtools.base

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {
    companion object {
        @Provides
        @Singleton
        internal fun provideSettings(@ApplicationContext context: Context) = Settings.getInstance(context)
    }
}
