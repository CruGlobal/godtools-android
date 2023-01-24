package org.keynote.godtools.android.db.repository

import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.keynote.godtools.android.db.GodToolsDao
import org.keynote.godtools.android.db.GodToolsDatabase

internal class GodToolsDaoRule(private val dispatcher: CoroutineDispatcher? = null) : TestWatcher() {
    private lateinit var db: GodToolsDatabase
    lateinit var dao: GodToolsDao

    override fun starting(description: Description?) {
        db = GodToolsDatabase(ApplicationProvider.getApplicationContext(), mockk())
        dao = GodToolsDao(db)
        if (dispatcher != null) dao.getService(CoroutineDispatcher::class.java) { dispatcher }
    }

    override fun finished(description: Description?) {
        db.close()
    }
}
