package org.cru.godtools.tract.liveshare

internal sealed class State {
    object On : State()
    object Off : State()
}

internal sealed class Event {
    object Start : Event()
    object Stop : Event()
}
