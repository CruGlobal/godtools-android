package org.cru.godtools.db.room.repository

import androidx.room.Dao
import kotlinx.coroutines.flow.map
import org.cru.godtools.db.repository.UserRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.entity.UserEntity
import org.cru.godtools.model.User

@Dao
internal abstract class UserRoomRepository(private val db: GodToolsRoomDatabase) : UserRepository {
    private val dao get() = db.userDao

    override fun getUserFlow(userId: String) = dao.findUserFlow(userId).map { it?.toModel() }

    override suspend fun storeUserFromSync(user: User) {
        dao.insertOrReplace(UserEntity(user))
    }
}
