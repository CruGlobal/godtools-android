package org.cru.godtools.db.room.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.ccci.gto.android.common.androidx.room.RoomDatabaseRule
import org.cru.godtools.db.repository.UserCountersRepositoryIT
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserCountersRoomRepositoryIT : UserCountersRepositoryIT() {
    @get:Rule
    internal val dbRule = RoomDatabaseRule(GodToolsRoomDatabase::class.java)
    override val repository get() = dbRule.db.userCountersRepository
}
