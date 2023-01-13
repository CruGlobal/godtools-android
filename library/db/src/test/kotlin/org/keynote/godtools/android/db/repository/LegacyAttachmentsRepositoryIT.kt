package org.keynote.godtools.android.db.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.BeforeTest
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.AttachmentsRepositoryIT
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class LegacyAttachmentsRepositoryIT : AttachmentsRepositoryIT() {
    @get:Rule
    val dbRule = GodToolsDaoRule()

    override lateinit var repository: AttachmentsRepository

    @BeforeTest
    fun setup() {
        repository = LegacyAttachmentsRepository(dbRule.dao)
    }
}
