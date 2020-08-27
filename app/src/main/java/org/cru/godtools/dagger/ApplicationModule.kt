package org.cru.godtools.dagger

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module(includes = [AndroidInjectionModule::class])
@InstallIn(SingletonComponent::class)
abstract class ApplicationModule {
    @Binds
    abstract fun context(@ApplicationContext context: Context): Context
}
