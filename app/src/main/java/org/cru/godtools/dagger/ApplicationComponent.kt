package org.cru.godtools.dagger

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import org.cru.godtools.GodToolsApplication
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        ApplicationModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<GodToolsApplication>
