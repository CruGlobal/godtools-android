package org.keynote.godtools.android.db.repository

import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.keynote.godtools.android.db.GodToolsDao
import org.keynote.godtools.android.db.GodToolsDatabase

internal class GodToolsDaoRule : TestWatcher() {
    private lateinit var db: GodToolsDatabase
    lateinit var dao: GodToolsDao

    override fun starting(description: Description?) {
        db = GodToolsDatabase(ApplicationProvider.getApplicationContext(), mockk())
        dao = GodToolsDao(db)
    }

    override fun finished(description: Description?) {
        db.close()
    }
}
