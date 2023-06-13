package org.cru.godtools.db.room.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.StandardTestDispatcher
import org.ccci.gto.android.common.androidx.room.RoomDatabaseRule
import org.cru.godtools.db.repository.TranslationsRepositoryIT
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class TranslationsRoomRepositoryIT : TranslationsRepositoryIT() {
    @get:Rule
    internal val dbRule = RoomDatabaseRule(
        GodToolsRoomDatabase::class.java,
        StandardTestDispatcher(testScope.testScheduler),
    )
    override val repository get() = dbRule.db.translationsRepository
    override val toolsRepository get() = dbRule.db.toolsRepository
    override val languagesRepository get() = dbRule.db.languagesRepository
}
