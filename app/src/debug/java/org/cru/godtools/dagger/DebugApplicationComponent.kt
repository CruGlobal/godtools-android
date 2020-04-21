package org.cru.godtools.dagger

import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        ApplicationModule::class,
        DebugApplicationModule::class
    ]
)
interface DebugApplicationComponent : ApplicationComponent
