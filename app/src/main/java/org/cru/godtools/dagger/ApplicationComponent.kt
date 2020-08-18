package org.cru.godtools.dagger

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton
import org.cru.godtools.GodToolsApplication

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        ApplicationModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<GodToolsApplication>
