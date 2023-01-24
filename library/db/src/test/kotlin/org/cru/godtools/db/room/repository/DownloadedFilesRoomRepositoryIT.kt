package org.cru.godtools.db.room.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.ccci.gto.android.common.androidx.room.RoomDatabaseRule
import org.cru.godtools.db.repository.DownloadedFilesRepositoryIT
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class DownloadedFilesRoomRepositoryIT : DownloadedFilesRepositoryIT() {
    @get:Rule
    internal val dbRule = RoomDatabaseRule(
        GodToolsRoomDatabase::class.java,
        StandardTestDispatcher(testScope.testScheduler)
    )
    override val repository get() = dbRule.db.downloadedFilesRepository

    @Test
    @Ignore
    override fun `getDownloadedTranslationFiles() & insertOrIgnore() & delete()`() = Unit
}
