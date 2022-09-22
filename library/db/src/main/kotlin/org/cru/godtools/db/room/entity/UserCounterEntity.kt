package org.cru.godtools.db.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.cru.godtools.model.UserCounter

@Entity(tableName = "user_counters")
internal class UserCounterEntity(
    @PrimaryKey
    val name: String,
    val count: Int = 0,
    val decayedCount: Double = 0.0,
    @ColumnInfo(defaultValue = "0")
    val delta: Int = 0,
) {
    fun toModel() = UserCounter(name).also {
        it.delta = delta
        it.apiCount = count
        it.apiDecayedCount = decayedCount
    }
}
