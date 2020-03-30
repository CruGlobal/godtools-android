package org.cru.godtools.ui

import dagger.Module
import org.cru.godtools.ui.profile.ProfileModule

@Module(
    includes = [
        ProfileModule::class
    ]
)
abstract class UiModule
