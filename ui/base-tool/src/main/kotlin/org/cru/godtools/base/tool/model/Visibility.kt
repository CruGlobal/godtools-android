package org.cru.godtools.base.tool.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.cru.godtools.shared.renderer.state.State
import org.cru.godtools.shared.tool.parser.model.Visibility

internal fun Visibility.isGoneStateFlow(state: State, scope: CoroutineScope) =
    isGoneFlow(state).stateIn(scope, SharingStarted.WhileSubscribed(5_000), isGone(state))
internal fun Visibility.isInvisibleStateFlow(state: State, scope: CoroutineScope) =
    isInvisibleFlow(state).stateIn(scope, SharingStarted.WhileSubscribed(5_000), isInvisible(state))
