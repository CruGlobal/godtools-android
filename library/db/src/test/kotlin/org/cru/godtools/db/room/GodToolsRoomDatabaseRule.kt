package org.cru.godtools.db.room

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestWatcher
import org.junit.runner.Description

internal class GodToolsRoomDatabaseRule : TestWatcher() {
    lateinit var db: GodToolsRoomDatabase

    override fun starting(description: Description?) {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), GodToolsRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    override fun finished(description: Description?) {
        db.close()
    }
}
