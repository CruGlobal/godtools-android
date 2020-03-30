package org.cru.godtools.ui

import dagger.Module
import org.cru.godtools.ui.languages.LanguagesModule
import org.cru.godtools.ui.profile.ProfileModule

@Module(
    includes = [
        LanguagesModule::class,
        ProfileModule::class
    ]
)
abstract class UiModule
