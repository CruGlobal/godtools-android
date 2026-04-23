package org.cru.godtools.ui.banner

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.cru.godtools.ui.banner.favoritetools.FavoriteToolsBannerPresenter
import org.cru.godtools.ui.banner.tutorial.TutorialFeaturesBannerPresenter

@Module
@InstallIn(SingletonComponent::class)
interface BannerModule {
    @Binds
    fun favoriteToolsBannerPresenter(
        presenter: FavoriteToolsBannerPresenter,
    ): BannerPresenter<FavoriteToolsBannerPresenter.UiState>

    @Binds
    fun tutorialFeaturesBannerPresenter(
        presenter: TutorialFeaturesBannerPresenter,
    ): BannerPresenter<TutorialFeaturesBannerPresenter.UiState>
}
