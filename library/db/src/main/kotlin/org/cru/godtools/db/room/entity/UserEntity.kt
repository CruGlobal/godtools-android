package org.cru.godtools.db.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.Instant
import org.ccci.gto.android.common.androidx.room.converter.Java8TimeConverters
import org.cru.godtools.model.User

@Entity(tableName = "users")
@TypeConverters(Java8TimeConverters::class)
internal class UserEntity(
    @PrimaryKey
    val id: String,
    val ssoGuid: String?,
    val createdAt: Instant?,
) {
    constructor(user: User) : this(id = user.id, ssoGuid = user.ssoGuid, createdAt = user.createdAt)
    fun toModel() = User(id = id, ssoGuid = ssoGuid, createdAt = createdAt)
}
