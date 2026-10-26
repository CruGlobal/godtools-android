package org.cru.godtools.ui.dashboard.optinnotification

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class OptInNotificationModule {
    @Binds
    abstract fun optInNotificationController(impl: DefaultOptInNotificationController): OptInNotificationController

    @Binds
    abstract fun optInNotificationPresenter(impl: DefaultOptInNotificationPresenter): OptInNotificationPresenter
}
