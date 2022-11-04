package org.cru.godtools.db.room.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

@Entity(tableName = "training_tips")
internal class TrainingTipEntity(@PrimaryKey @Embedded val key: Key) {
    internal data class Key(val tool: String, val locale: Locale, val tipId: String)

    var isCompleted = false
    var isNew = true
}
