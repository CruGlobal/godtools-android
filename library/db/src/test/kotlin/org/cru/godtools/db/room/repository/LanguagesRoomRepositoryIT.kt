package org.cru.godtools.db.room.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.StandardTestDispatcher
import org.ccci.gto.android.common.androidx.room.RoomDatabaseRule
import org.cru.godtools.db.repository.LanguagesRepositoryIT
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class LanguagesRoomRepositoryIT : LanguagesRepositoryIT() {
    @get:Rule
    internal val dbRule = RoomDatabaseRule(
        GodToolsRoomDatabase::class.java,
        StandardTestDispatcher(testScope.testScheduler)
    )
    override val repository get() = dbRule.db.languagesRepository
    override val toolsRepository get() = dbRule.db.toolsRepository
    override val translationsRepository get() = dbRule.db.translationsRepository
}
