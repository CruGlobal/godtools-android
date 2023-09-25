package org.cru.godtools.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.db.room.entity.UserEntity

@Dao
internal interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun findUser(userId: String): UserEntity?
    @Query("SELECT * FROM users WHERE id = :userId")
    fun findUserFlow(userId: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(user: UserEntity)
}
