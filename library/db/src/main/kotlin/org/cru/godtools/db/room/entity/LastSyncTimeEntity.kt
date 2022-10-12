package org.cru.godtools.db.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "last_sync_times")
internal class LastSyncTimeEntity(@PrimaryKey val id: String, val time: Long = System.currentTimeMillis()) {
    constructor(key: Array<out Any>) : this(flattenKey(key))

    companion object {
        const val KEY_SEPARATOR = ":"
        fun flattenKey(key: Array<out Any>) = key.joinToString(KEY_SEPARATOR)
    }
}
