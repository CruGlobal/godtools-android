package org.cru.godtools.db.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import org.cru.godtools.model.User

@Entity(tableName = "users")
internal class UserEntity(
    @PrimaryKey
    val id: String,
    val ssoGuid: String?,
    val name: String?,
    val givenName: String?,
    val familyName: String?,
    val email: String?,
    val createdAt: Instant?,
) {
    constructor(user: User) : this(
        id = user.id,
        ssoGuid = user.ssoGuid,
        name = user.name,
        givenName = user.givenName,
        familyName = user.familyName,
        email = user.email,
        createdAt = user.createdAt,
    )

    fun toModel() = User(
        id = id,
        ssoGuid = ssoGuid,
        name = name,
        givenName = givenName,
        familyName = familyName,
        email = email,
        createdAt = createdAt,
    )
}
