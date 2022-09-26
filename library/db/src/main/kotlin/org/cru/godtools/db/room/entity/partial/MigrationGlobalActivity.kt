package org.cru.godtools.db.room.entity.partial

import org.cru.godtools.db.room.entity.GlobalActivityEntity

internal class MigrationGlobalActivity(
    val users: Int = 0,
    val countries: Int = 0,
    val launches: Int = 0,
    val gospelPresentations: Int = 0,
) {
    val id = GlobalActivityEntity.ID
}
