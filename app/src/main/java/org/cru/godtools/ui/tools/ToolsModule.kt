package org.cru.godtools.ui.tools

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.cru.godtools.fragment.ToolsFragment

@Module
abstract class ToolsModule {
    @ContributesAndroidInjector
    internal abstract fun toolsFragmentInjector(): ToolsFragment
}
