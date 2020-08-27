package org.cru.godtools.dagger

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.cru.godtools.base.SettingsModule
import org.cru.godtools.ui.UiModule

@InstallIn(SingletonComponent::class)
@Module(
    includes = [
        AndroidInjectionModule::class,
        ConfigModule::class,
        ServicesModule::class,
        SettingsModule::class,
        UiModule::class
    ]
)
abstract class ApplicationModule {
    @Binds
    abstract fun context(@ApplicationContext context: Context): Context
}
