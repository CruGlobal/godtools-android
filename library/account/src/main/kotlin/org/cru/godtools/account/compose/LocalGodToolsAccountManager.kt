package org.cru.godtools.account.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import org.cru.godtools.account.GodToolsAccountManager

internal object LocalGodToolsAccountManager {
    private val LocalComposition = compositionLocalOf<GodToolsAccountManager?> { null }

    /**
     * Returns current composition local value for the manager or fallback to resolution using Dagger.
     */
    val current: GodToolsAccountManager
        @Composable
        get() = LocalComposition.current
            ?: EntryPointAccessors.fromApplication<AccountEntryPoint>(LocalContext.current).accountManager

    /**
     * Associates a [GodToolsAccountManager] key to a value in a call to [CompositionLocalProvider].
     */
    infix fun provides(accountManager: GodToolsAccountManager): ProvidedValue<GodToolsAccountManager?> {
        return LocalComposition.provides(accountManager)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface AccountEntryPoint {
    val accountManager: GodToolsAccountManager
}
