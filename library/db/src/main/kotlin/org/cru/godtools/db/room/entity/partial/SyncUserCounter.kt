package org.cru.godtools.db.room.entity.partial

import org.cru.godtools.model.UserCounter

internal class SyncUserCounter(val name: String, val count: Int, val decayedCount: Double) {
    constructor(counter: UserCounter) : this(counter.name, counter.apiCount, counter.apiDecayedCount)
}
