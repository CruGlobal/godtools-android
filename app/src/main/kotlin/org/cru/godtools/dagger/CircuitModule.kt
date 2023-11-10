package org.cru.godtools.dagger

import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds

@Module
@InstallIn(SingletonComponent::class)
abstract class CircuitModule {
    @Multibinds
    abstract fun presenterFactories(): Set<Presenter.Factory>

    @Multibinds
    abstract fun uiFactories(): Set<Ui.Factory>

    companion object {
        @Provides
        @Reusable
        fun circuit(
            presenterFactories: Set<@JvmSuppressWildcards Presenter.Factory>,
            uiFactories: Set<@JvmSuppressWildcards Ui.Factory>
        ) = Circuit.Builder()
            .addPresenterFactories(presenterFactories)
            .addUiFactories(uiFactories)
            .build()
    }
}
