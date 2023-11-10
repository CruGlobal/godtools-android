package org.cru.godtools.ui.account.delete

import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

// TODO: switch to codegen for this once https://github.com/slackhq/circuit/pull/963 is released
@Module
@InstallIn(SingletonComponent::class)
object DeleteAccountModule {
    @Provides
    @Reusable
    @IntoSet
    fun presenterFactory(factory: DeleteAccountPresenter.Factory) = Presenter.Factory { screen, navigator, _ ->
        when (screen) {
            DeleteAccountScreen -> factory.create(navigator = navigator)
            else -> null
        }
    }

    @Provides
    @Reusable
    @IntoSet
    fun uiFactory() = Ui.Factory { screen, _ ->
        when (screen) {
            DeleteAccountScreen -> ui<DeleteAccountScreen.State> { state, modifier ->
                DeleteAccountLayout(state = state, modifier = modifier)
            }
            else -> null
        }
    }
}
