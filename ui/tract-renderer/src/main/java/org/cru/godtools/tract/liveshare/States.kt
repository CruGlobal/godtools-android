package org.cru.godtools.tract.liveshare

internal sealed class State {
    object Initial : State()
    object Starting : State()
    object On : State()
    object Off : State()
}

internal sealed class Event {
    object Start : Event()
    object Started : Event()
    object Stop : Event()
}

internal sealed class SideEffect {
    object OpenSocket : SideEffect()
}
