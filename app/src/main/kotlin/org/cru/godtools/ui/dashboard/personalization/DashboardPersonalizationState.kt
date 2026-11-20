@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package org.cru.godtools.ui.dashboard.personalization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Personalization state shared between the dashboard app bar and the Tools & Lessons pages.
 *
 * The pages are parameterless Circuit screens, so this is how the dashboard hands them state it also needs to render.
 */
@Stable
class DashboardPersonalizationState(
    val isEnabled: Boolean = false,
    toolsPersonalized: Boolean = isEnabled,
    lessonsPersonalized: Boolean = isEnabled,
) {
    var toolsPersonalized by mutableStateOf(toolsPersonalized)
    var lessonsPersonalized by mutableStateOf(lessonsPersonalized)

    companion object {
        val Saver: Saver<DashboardPersonalizationState, Any> = listSaver(
            save = { listOf(it.isEnabled, it.toolsPersonalized, it.lessonsPersonalized) },
            restore = { DashboardPersonalizationState(it[0], it[1], it[2]) },
        )
    }
}

@Composable
internal fun rememberDashboardPersonalizationState(isEnabled: () -> Boolean) =
    rememberSaveable(saver = DashboardPersonalizationState.Saver) { DashboardPersonalizationState(isEnabled()) }

internal val LocalDashboardPersonalizationState = staticCompositionLocalOf { DashboardPersonalizationState() }
