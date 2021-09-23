package org.cru.godtools.tool.lesson

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.cru.godtools.analytics.appsflyer.AppsFlyerDeepLinkResolver
import org.cru.godtools.tool.lesson.analytics.appsflyer.LessonAppsFlyerDeepLinkResolver

@Module
@InstallIn(SingletonComponent::class)
object LessonModule {
    @IntoSet
    @Provides
    @Reusable
    fun lessonAppsFlyerDeepLinkResolver(): AppsFlyerDeepLinkResolver = LessonAppsFlyerDeepLinkResolver
}
