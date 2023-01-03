package org.keynote.godtools.android.db.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.BeforeTest
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.ToolsRepositoryIT
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class LegacyToolsRepositoryIT : ToolsRepositoryIT() {
    @get:Rule
    val dbRule = GodToolsDaoRule()

    override lateinit var repository: ToolsRepository

    @BeforeTest
    fun setup() {
        repository = LegacyToolsRepository(dbRule.dao)
    }
}
