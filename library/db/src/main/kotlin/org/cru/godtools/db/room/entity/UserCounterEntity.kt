package org.cru.godtools.db.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_counters")
internal class UserCounterEntity(
    @PrimaryKey
    val name: String,
    val count: Int = 0,
    val decayedCount: Double = 0.0,
    val delta: Int = 0,
)
