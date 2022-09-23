package org.cru.godtools.db.room.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.db.repository.UserCountersRepositoryTest
import org.cru.godtools.db.room.GodToolsRoomDatabaseRule
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserCountersRoomRepositoryIT : UserCountersRepositoryTest() {
    @get:Rule
    internal val dbRule = GodToolsRoomDatabaseRule()
    override val repository get() = dbRule.db.userCountersRepository
}
