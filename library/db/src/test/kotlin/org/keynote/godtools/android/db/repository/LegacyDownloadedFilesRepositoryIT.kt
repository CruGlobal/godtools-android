package org.keynote.godtools.android.db.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.BeforeTest
import kotlinx.coroutines.test.StandardTestDispatcher
import org.ccci.gto.android.common.androidx.room.RoomDatabaseRule
import org.cru.godtools.db.repository.DownloadedFilesRepository
import org.cru.godtools.db.repository.DownloadedFilesRepositoryIT
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class LegacyDownloadedFilesRepositoryIT : DownloadedFilesRepositoryIT() {
    @get:Rule
    val dbRule = GodToolsDaoRule(StandardTestDispatcher(testScope.testScheduler))
    @get:Rule
    internal val roomDbRule = RoomDatabaseRule(
        GodToolsRoomDatabase::class.java,
        StandardTestDispatcher(testScope.testScheduler)
    )
    override lateinit var repository: DownloadedFilesRepository

    @BeforeTest
    fun setup() {
        repository = LegacyDownloadedFilesRepository(dbRule.dao, roomDbRule.db)
    }
}
