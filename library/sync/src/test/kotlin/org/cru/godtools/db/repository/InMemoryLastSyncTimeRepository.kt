package org.cru.godtools.db.repository

// TODO: this should be a testFixture in :library:db once Kotlin Android Test Fixtures are supported
//       see: https://youtrack.jetbrains.com/issue/KT-50667
class InMemoryLastSyncTimeRepository : LastSyncTimeRepository {
    val entries = mutableMapOf<List<Any>, Long>()

    override suspend fun getLastSyncTime(vararg key: Any) = entries[key.toList()] ?: 0

    override suspend fun isLastSyncStale(vararg key: Any, staleAfter: Long): Boolean {
        val time = entries[key.toList()] ?: return true
        return time + staleAfter < System.currentTimeMillis()
    }

    override suspend fun updateLastSyncTime(vararg key: Any) {
        entries[key.toList()] = System.currentTimeMillis()
    }

    override suspend fun resetLastSyncTime(vararg key: Any, isPrefix: Boolean) {
        entries.remove(key.toList())
        if (isPrefix) {
            entries.keys.removeIf { key.toList() == it.take(key.size) }
        }
    }
}
