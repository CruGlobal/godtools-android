package org.cru.godtools.db.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.Locale
import org.cru.godtools.model.Base
import org.cru.godtools.model.Followup

@Entity(tableName = "followups")
internal class FollowupEntity(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    val name: String?,
    val email: String,
    val destination: Long,
    val language: Locale,
    val createdAt: Instant,
) {
    constructor(model: Followup) : this(
        id = model.id.takeUnless { it == Base.INVALID_ID },
        name = model.name,
        email = model.email,
        destination = model.destination,
        language = model.languageCode,
        createdAt = model.createTime
    )

    fun toModel() = Followup(
        id = id,
        destination = destination,
        languageCode = language,
        name = name,
        email = email,
        createTime = createdAt
    )
}
