package org.cru.godtools.ui

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.cru.godtools.tract.TractRendererModule
import org.cru.godtools.ui.about.AboutModule
import org.cru.godtools.ui.languages.LanguagesModule
import org.cru.godtools.ui.profile.ProfileModule
import org.cru.godtools.ui.tooldetails.ToolDetailsModule
import org.cru.godtools.ui.tools.ToolsModule
import org.keynote.godtools.android.activity.MainActivity

@Module(
    includes = [
        AboutModule::class,
        LanguagesModule::class,
        ProfileModule::class,
        ToolsModule::class,
        ToolDetailsModule::class,
        TractRendererModule::class
    ]
)
abstract class UiModule {
    @ContributesAndroidInjector
    internal abstract fun mainActivityInjector(): MainActivity
}
