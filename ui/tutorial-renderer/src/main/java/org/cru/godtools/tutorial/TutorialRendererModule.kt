package org.cru.godtools.tutorial

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.cru.godtools.tutorial.activity.TutorialActivity

@Module
abstract class TutorialRendererModule {
    @ContributesAndroidInjector
    internal abstract fun tutorialActivityInjector(): TutorialActivity
}
