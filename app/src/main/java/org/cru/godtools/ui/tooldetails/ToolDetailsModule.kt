package org.cru.godtools.ui.tooldetails

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ToolDetailsModule {
    @ContributesAndroidInjector
    internal abstract fun toolDetailsFragmentInjector(): ToolDetailsFragment
}
