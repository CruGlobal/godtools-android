package org.cru.godtools.article

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.cru.godtools.analytics.appsflyer.AppsFlyerDeepLinkResolver
import org.cru.godtools.article.analytics.appsflyer.ArticlesAppsFlyerDeepLinkResolver

@Module
@InstallIn(SingletonComponent::class)
object ArticlesModule {
    @IntoSet
    @Provides
    @Reusable
    fun articlesAppsFlyerDeepLinkResolver(): AppsFlyerDeepLinkResolver = ArticlesAppsFlyerDeepLinkResolver
}
