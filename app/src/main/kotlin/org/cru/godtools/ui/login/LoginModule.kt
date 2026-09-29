package org.cru.godtools.ui.login

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LoginModule {
    @Binds
    abstract fun loginLauncherProducer(impl: DefaultLoginLauncherProducer): LoginLauncherProducer
}
