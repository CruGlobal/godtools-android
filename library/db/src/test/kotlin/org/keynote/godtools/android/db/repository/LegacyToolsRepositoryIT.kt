package org.keynote.godtools.android.db.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.BeforeTest
import kotlinx.coroutines.test.StandardTestDispatcher
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.ToolsRepositoryIT
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class LegacyToolsRepositoryIT : ToolsRepositoryIT() {
    @get:Rule
    val dbRule = GodToolsDaoRule(StandardTestDispatcher(testScope.testScheduler))

    override lateinit var repository: ToolsRepository
    override lateinit var attachmentsRepository: AttachmentsRepository

    @BeforeTest
    fun setup() {
        attachmentsRepository = LegacyAttachmentsRepository(dbRule.dao)
        repository = LegacyToolsRepository(dbRule.dao, attachmentsRepository)
    }
}
