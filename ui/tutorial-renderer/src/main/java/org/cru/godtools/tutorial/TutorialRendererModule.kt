package org.cru.godtools.tutorial

import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.cru.godtools.tutorial.activity.TutorialActivity

@Module
@InstallIn(SingletonComponent::class)
abstract class TutorialRendererModule {
    @ContributesAndroidInjector
    internal abstract fun tutorialActivityInjector(): TutorialActivity
}
