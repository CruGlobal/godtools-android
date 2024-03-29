package org.cru.godtools.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.db.room.entity.GlobalActivityEntity
import org.cru.godtools.db.room.entity.partial.MigrationGlobalActivity

@Dao
internal interface GlobalActivityDao {
    @Query("SELECT * FROM global_activity WHERE id = ${GlobalActivityEntity.ID}")
    fun findGlobalActivityFlow(): Flow<GlobalActivityEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(activity: GlobalActivityEntity)

    @Insert(entity = GlobalActivityEntity::class, onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(activity: MigrationGlobalActivity)
}
