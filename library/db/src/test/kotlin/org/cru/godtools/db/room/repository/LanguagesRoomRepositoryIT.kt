package org.cru.godtools.db.room.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.cru.godtools.db.repository.LanguagesRepositoryIT
import org.cru.godtools.db.room.GodToolsRoomDatabaseRule
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class LanguagesRoomRepositoryIT : LanguagesRepositoryIT() {
    @get:Rule
    internal val dbRule = GodToolsRoomDatabaseRule(StandardTestDispatcher(testScope.testScheduler))
    override val repository get() = dbRule.db.languagesRepository
}
