package org.cru.godtools.ui

import dagger.Module
import org.cru.godtools.article.ArticleRendererModule
import org.cru.godtools.ui.languages.LanguagesModule
import org.cru.godtools.ui.profile.ProfileModule
import org.cru.godtools.ui.tooldetails.ToolDetailsModule
import org.cru.godtools.ui.tools.ToolsModule

@Module(
    includes = [
        ArticleRendererModule::class,
        LanguagesModule::class,
        ProfileModule::class,
        ToolsModule::class,
        ToolDetailsModule::class
    ]
)
abstract class UiModule
