package org.cru.godtools.db.room

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import org.junit.rules.TestWatcher
import org.junit.runner.Description

internal class GodToolsRoomDatabaseRule(private val dispatcher: CoroutineDispatcher? = null) : TestWatcher() {
    lateinit var db: GodToolsRoomDatabase

    override fun starting(description: Description?) {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), GodToolsRoomDatabase::class.java)
            .allowMainThreadQueries()
            .apply { if (dispatcher != null) setQueryExecutor(dispatcher.asExecutor()) }
            .build()
    }

    override fun finished(description: Description?) {
        db.close()
    }
}
