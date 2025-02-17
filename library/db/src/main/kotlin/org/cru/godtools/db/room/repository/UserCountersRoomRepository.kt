package org.cru.godtools.db.room.repository

import androidx.room.Dao
import androidx.room.Transaction
import androidx.room.withTransaction
import kotlinx.coroutines.flow.map
import org.cru.godtools.db.repository.UserCountersRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.entity.UserCounterEntity
import org.cru.godtools.db.room.entity.partial.MigrationUserCounter
import org.cru.godtools.db.room.entity.partial.SyncUserCounter
import org.cru.godtools.model.UserCounter

@Dao
internal abstract class UserCountersRoomRepository(private val db: GodToolsRoomDatabase) : UserCountersRepository {
    private val dao get() = db.userCountersDao

    override suspend fun <R> transaction(block: suspend () -> R) = db.withTransaction(block)

    override suspend fun getCounters() = dao.getUserCounters().map { it.toModel() }
    override fun getCountersFlow() = dao.getUserCountersFlow().map { it.map { it.toModel() } }

    @Transaction
    override suspend fun updateCounter(name: String, delta: Int) {
        dao.insertOrIgnore(UserCounterEntity(name))
        if (delta != 0) dao.updateUserCounterDelta(name, delta)
    }

    // region Sync methods
    override suspend fun getDirtyCounters() = dao.getDirtyCounters().map { it.toModel() }

    @Transaction
    override suspend fun storeCountersFromSync(counters: Collection<UserCounter>) {
        dao.upsert(counters.map { SyncUserCounter(it) })
    }

    override suspend fun resetCountersMissingFromSync(counters: Collection<UserCounter>) {
        dao.update(counters.map { SyncUserCounter(it.name, count = 0, decayedCount = 0.0) })
    }
    // endregion Sync methods

    // region migration
    @Transaction
    internal open fun migrateCounter(name: String, count: Int, decayedCount: Double, delta: Int) {
        val migrationCounter = MigrationUserCounter(name, count, decayedCount)
        dao.insertOrIgnore(migrationCounter)
        dao.update(migrationCounter)
        dao.migrateDelta(name, delta)
    }
    // endregion migration
}
